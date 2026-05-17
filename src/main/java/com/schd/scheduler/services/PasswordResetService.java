package com.schd.scheduler.services;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schd.scheduler.repositories.UserRepository;
import com.schd.scheduler.generated.tables.PasswordResetTokens;
import com.schd.scheduler.generated.tables.records.PasswordResetTokensRecord;
import com.schd.scheduler.generated.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    private static final long TOKEN_EXPIRY_HOURS = 1;

    @Transactional
    public void requestPasswordReset(String email) {
        UsersRecord user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        String token = generateToken();
        saveResetToken(user.getId(), token);

        sendResetEmail(user.getEmail(), token);
        log.info("Password reset requested for email: {}", email);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        validateToken(token);

        UsersRecord user = findUserByToken(token);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.update(user);

        deleteResetToken(token);
        log.info("Password reset completed for user: {}", user.getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void saveResetToken(UUID userId, String token) {
        var dsl = userRepository.dsl();
        var now = OffsetDateTime.now();

        PasswordResetTokensRecord record = new PasswordResetTokensRecord();
        record.setUserId(userId);
        record.setToken(token);
        record.setExpiresAt(now.plusHours(TOKEN_EXPIRY_HOURS));
        record.setCreatedAt(now);

        dsl.insertInto(PasswordResetTokens.PASSWORD_RESET_TOKENS)
            .set(record)
            .execute();
    }

    private void validateToken(String token) {
        var dsl = userRepository.dsl();
        
        PasswordResetTokensRecord record = dsl.selectFrom(PasswordResetTokens.PASSWORD_RESET_TOKENS)
            .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN.eq(token))
            .fetchOptional()
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (record.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }
    }

    private UsersRecord findUserByToken(String token) {
        var dsl = userRepository.dsl();
        
        UUID userId = dsl.select(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID)
            .from(PasswordResetTokens.PASSWORD_RESET_TOKENS)
            .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN.eq(token))
            .fetchOne(PasswordResetTokens.PASSWORD_RESET_TOKENS.USER_ID);

        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void deleteResetToken(String token) {
        var dsl = userRepository.dsl();
        dsl.deleteFrom(PasswordResetTokens.PASSWORD_RESET_TOKENS)
            .where(PasswordResetTokens.PASSWORD_RESET_TOKENS.TOKEN.eq(token))
            .execute();
    }

    private void sendResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Your password reset token: " + token + "\n\nThis token expires in 1 hour.");
        message.setFrom("noreply@schedulerflow.com");

        try {
            mailSender.send(message);
            log.debug("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.warn("Failed to send password reset email, printing token to console for: {}", email);
            System.out.println("Password reset token for " + email + ": " + token);
        }
    }
}