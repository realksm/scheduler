package com.schd.scheduler.services;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schd.scheduler.dtos.AuthResponse;
import com.schd.scheduler.repositories.UserRepository;
import com.schd.scheduler.generated.tables.RefreshTokens;
import com.schd.scheduler.generated.tables.records.RefreshTokensRecord;
import com.schd.scheduler.generated.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    @Transactional
    public AuthResponse generateRefreshToken(UsersRecord user) {
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user.getId());

        log.debug("Generated refresh token for user {}", user.getId());
        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getUsername());
    }

    @Transactional
    public AuthResponse refreshAccessToken(String refreshToken) {
        UUID userId = validateRefreshToken(refreshToken);

        UsersRecord user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String newAccessToken = jwtService.generateToken(user.getId(), user.getEmail());
        log.debug("Refreshed access token for user {}", userId);

        return new AuthResponse(newAccessToken, refreshToken, user.getId(), user.getEmail(), user.getUsername());
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        var dsl = userRepository.dsl();
        int deleted = dsl.deleteFrom(RefreshTokens.REFRESH_TOKENS)
            .where(RefreshTokens.REFRESH_TOKENS.TOKEN.eq(refreshToken))
            .execute();

        if (deleted > 0) {
            log.info("Revoked refresh token");
        }
    }

    private String createRefreshToken(UUID userId) {
        String token = generateToken();
        var dsl = userRepository.dsl();
        var now = OffsetDateTime.now();

        RefreshTokensRecord record = new RefreshTokensRecord();
        record.setUserId(userId);
        record.setToken(token);
        record.setExpiresAt(now.plusDays(REFRESH_TOKEN_EXPIRY_DAYS));
        record.setCreatedAt(now);

        dsl.insertInto(RefreshTokens.REFRESH_TOKENS)
            .set(record)
            .execute();

        return token;
    }

    private UUID validateRefreshToken(String token) {
        var dsl = userRepository.dsl();
        
        RefreshTokensRecord record = dsl.selectFrom(RefreshTokens.REFRESH_TOKENS)
            .where(RefreshTokens.REFRESH_TOKENS.TOKEN.eq(token))
            .fetchOptional()
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (record.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        return record.getUserId();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}