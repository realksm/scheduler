package com.schd.scheduler.services;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.schd.scheduler.dtos.AuthResponse;
import com.schd.scheduler.dtos.LoginRequest;
import com.schd.scheduler.dtos.RegisterRequest;
import com.schd.scheduler.repositories.UserRepository;
import com.schd.scheduler.generated.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;

import static org.jooq.impl.DSL.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 7;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }

        UsersRecord user = new UsersRecord();
        user.setId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setTimezone("UTC");
        user.setLocale("en");
        user.setEmailVerified(false);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        userRepository.create(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user.getId());
        
        return new AuthResponse(token, refreshToken, user.getId(), user.getEmail(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        UsersRecord user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        String refreshToken = createRefreshToken(user.getId());
        
        return new AuthResponse(token, refreshToken, user.getId(), user.getEmail(), user.getUsername());
    }

    private String createRefreshToken(UUID userId) {
        String token = generateToken();
        var dsl = userRepository.dsl();
        var now = OffsetDateTime.now();

        dsl.insertInto(table("refresh_tokens"))
            .set(field("user_id"), userId)
            .set(field("token"), token)
            .set(field("expires_at"), now.plusDays(REFRESH_TOKEN_EXPIRY_DAYS))
            .set(field("created_at"), now)
            .execute();

        return token;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}