package com.schd.scheduler.services;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schd.scheduler.dtos.AuthResponse;
import com.schd.scheduler.repositories.UserRepository;
import com.schd.scheduler.generated.tables.OauthAccounts;
import com.schd.scheduler.generated.tables.Users;
import com.schd.scheduler.generated.tables.records.OauthAccountsRecord;
import com.schd.scheduler.generated.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    private static final String ATTR_ID = "id";
    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_LOGIN = "login";
    private static final String ATTR_NAME = "name";

    /**
     * This method is called by Spring Security during OAuth2 login.
     * It loads user info from the OAuth provider and creates/links local user.
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerUserId = getProviderUserId(oauth2User, provider);
        
        log.info("OAuth2 login attempt for provider: {}, userId: {}", provider, providerUserId);
        
        UsersRecord user = resolveUser(provider, providerUserId, oauth2User);
        
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        
        AuthResponse authResponse = new AuthResponse(token, null, user.getId(), user.getEmail(), user.getUsername());
        
        return new OAuth2UserWithToken(oauth2User, authResponse);
    }

    /**
     * Called from SecurityConfig success handler to get the JWT response.
     */
    public AuthResponse handleOAuthLogin(String provider, String providerUserId, OAuth2User oauth2User) {
        UsersRecord user = resolveUser(provider, providerUserId, oauth2User);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, null, user.getId(), user.getEmail(), user.getUsername());
    }

    private String getProviderUserId(OAuth2User oauth2User, String provider) {
        if ("github".equals(provider)) {
            return String.valueOf(oauth2User.getAttribute(ATTR_ID));
        }
        return oauth2User.getAttribute("sub");
    }

    private UsersRecord resolveUser(String provider, String providerUserId, OAuth2User oauth2User) {
        var dsl = userRepository.dsl();

        OauthAccountsRecord existingOauth = dsl.selectFrom(OauthAccounts.OAUTH_ACCOUNTS)
            .where(OauthAccounts.OAUTH_ACCOUNTS.PROVIDER.eq(provider))
            .and(OauthAccounts.OAUTH_ACCOUNTS.PROVIDER_USER_ID.eq(providerUserId))
            .fetchOptional()
            .orElse(null);

        if (existingOauth != null && existingOauth.getUserId() != null) {
            return userRepository.findById(existingOauth.getUserId())
                .orElseThrow(() -> new IllegalStateException(
                    "oauth_accounts row exists but user " + existingOauth.getUserId() + " not found"));
        }

        String email = getEmail(oauth2User, provider);
        String name = resolveName(oauth2User, provider);

        UsersRecord user = dsl.selectFrom(Users.USERS)
            .where(Users.USERS.EMAIL.eq(email))
            .fetchOptional()
            .orElseGet(() -> createUser(dsl, oauth2User, email, name, provider, providerUserId));

        if (existingOauth == null) {
            OauthAccountsRecord oauthRecord = new OauthAccountsRecord();
            oauthRecord.setUserId(user.getId());
            oauthRecord.setProvider(provider);
            oauthRecord.setProviderUserId(providerUserId);
            oauthRecord.setCreatedAt(OffsetDateTime.now());

            dsl.insertInto(OauthAccounts.OAUTH_ACCOUNTS)
                .set(oauthRecord)
                .onConflictDoNothing()
                .execute();
        }

        return user;
    }

    private String getEmail(OAuth2User oauth2User, String provider) {
        String email = oauth2User.getAttribute(ATTR_EMAIL);
        
        if (email == null && "github".equals(provider)) {
            Object emailsAttr = oauth2User.getAttribute("emails");
            if (emailsAttr instanceof Iterable) {
                for (Object obj : (Iterable<?>) emailsAttr) {
                    if (obj instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> emailObj = (java.util.Map<String, Object>) obj;
                        Boolean primary = (Boolean) emailObj.get("primary");
                        if (Boolean.TRUE.equals(primary)) {
                            email = (String) emailObj.get("email");
                            break;
                        }
                    }
                }
            }
        }
        
        return email;
    }

    private UsersRecord createUser(org.jooq.DSLContext dsl, OAuth2User oauth2User, String email, String name,
                                   String provider, String providerUserId) {
        UUID userId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        String username = generateUsername(oauth2User, provider, email);

        UsersRecord user = new UsersRecord();
        user.setId(userId);
        user.setEmail(email);
        user.setUsername(username);
        user.setFullName(name != null ? name : username);
        user.setTimezone("UTC");
        user.setLocale("en");
        user.setEmailVerified(true);
        user.setOauthProvider(provider);
        user.setOauthProviderId(providerUserId);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        dsl.insertInto(Users.USERS)
            .set(user)
            .execute();

        log.info("Created new user {} via {} OAuth", userId, provider);

        return dsl.selectFrom(Users.USERS)
            .where(Users.USERS.ID.eq(userId))
            .fetchOne();
    }

    private String generateUsername(OAuth2User oauth2User, String provider, String email) {
        String baseUsername;
        
        if ("github".equals(provider)) {
            baseUsername = oauth2User.getAttribute(ATTR_LOGIN);
            if (baseUsername == null || baseUsername.isBlank()) {
                baseUsername = email.split("@")[0];
            }
        } else {
            baseUsername = email.split("@")[0];
        }
        
        baseUsername = baseUsername.replaceAll("[^a-zA-Z0-9_]", "_");
        
        return ensureUniqueUsername(baseUsername);
    }

    private String resolveName(OAuth2User oauth2User, String provider) {
        String name = oauth2User.getAttribute(ATTR_NAME);
        if (name != null && !name.isBlank()) return name.trim();

        String given = oauth2User.getAttribute("given_name");
        String family = oauth2User.getAttribute("family_name");
        
        if (given != null || family != null) {
            return ((given != null ? given : "") + " " + (family != null ? family : "")).trim();
        }
        
        return null;
    }

    private String ensureUniqueUsername(String base) {
        var dsl = userRepository.dsl();
        String candidate = base;
        int suffix = 2;
        
        while (dsl.fetchExists(
                dsl.selectFrom(Users.USERS).where(Users.USERS.USERNAME.eq(candidate)))) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }

    /**
     * Custom OAuth2User that wraps the original OAuth2User and carries our AuthResponse.
     */
    public static class OAuth2UserWithToken implements OAuth2User {
        private final OAuth2User delegate;
        private final AuthResponse authResponse;

        OAuth2UserWithToken(OAuth2User delegate, AuthResponse authResponse) {
            this.delegate = delegate;
            this.authResponse = authResponse;
        }

        public AuthResponse getAuthResponse() {
            return authResponse;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return delegate.getAttributes();
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return delegate.getAuthorities();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public <A> A getAttribute(String name) {
            return delegate.getAttribute(name);
        }
    }
}