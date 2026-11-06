
package com.backend.service;

import com.backend.entity.PasswordResetToken;
import com.backend.entity.User;
import com.backend.repository.PasswordResetTokenRepository;
import com.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            SessionService sessionService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    @Transactional
    public String createResetToken(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Unable to process request."));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Unable to process request.");
        }

        tokenRepository.deleteAllByUser_Id(user.getId());

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // Development/testing only: the raw token is returned to the caller.
        // Do not expose this through a production API.
        return rawToken;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Invalid or expired reset token.");
        }

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid or expired reset token."));

        if (resetToken.isUsed()
                || !resetToken.getExpiresAt().isAfter(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired reset token.");
        }

        User user = resetToken.getUser();

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Invalid or expired reset token.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        sessionService.revokeAllSessionsForUser(user.getId());

        // Invalidate every reset token previously issued to this user.
        tokenRepository.deleteAllByUser_Id(user.getId());
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash token.", ex);
        }
    }
}
