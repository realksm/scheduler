package com.schd.scheduler.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OAuth2ClientConfig {

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @Value("${GOOGLE_CLIENT_SECRET:}")
    private String googleClientSecret;

    @Value("${MICROSOFT_CLIENT_ID:}")
    private String microsoftClientId;

    @Value("${MICROSOFT_CLIENT_SECRET:}")
    private String microsoftClientSecret;

    @Value("${GITHUB_CLIENT_ID:}")
    private String githubClientId;

    @Value("${GITHUB_CLIENT_SECRET:}")
    private String githubClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        List<ClientRegistration> registrations = new ArrayList<>();

        if (!googleClientId.isBlank() && !googleClientSecret.isBlank()) {
            registrations.add(ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/google")
                .scope("profile", "email")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build());
            log.info("Google OAuth2 enabled");
        }

        if (!microsoftClientId.isBlank() && !microsoftClientSecret.isBlank()) {
            registrations.add(ClientRegistration.withRegistrationId("microsoft")
                .clientId(microsoftClientId)
                .clientSecret(microsoftClientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/microsoft")
                .scope("user.read")
                .authorizationUri("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                .tokenUri("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                .userInfoUri("https://graph.microsoft.com/v1.0/me")
                .userNameAttributeName("id")
                .clientName("Microsoft")
                .build());
            log.info("Microsoft OAuth2 enabled");
        }

        if (!githubClientId.isBlank() && !githubClientSecret.isBlank()) {
            registrations.add(ClientRegistration.withRegistrationId("github")
                .clientId(githubClientId)
                .clientSecret(githubClientSecret)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user", "user:email")
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com/user")
                .userNameAttributeName("id")
                .clientName("GitHub")
                .build());
            log.info("GitHub OAuth2 enabled");
        }

        if (registrations.isEmpty()) {
            log.info("No OAuth2 providers configured - OAuth2 login disabled");
        }

        return new InMemoryClientRegistrationRepository(registrations);
    }

    private static class InMemoryClientRegistrationRepository implements ClientRegistrationRepository {
        private final List<ClientRegistration> registrations;

        InMemoryClientRegistrationRepository(List<ClientRegistration> registrations) {
            this.registrations = registrations;
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return registrations.stream()
                .filter(r -> r.getRegistrationId().equals(registrationId))
                .findFirst()
                .orElse(null);
        }
    }
}