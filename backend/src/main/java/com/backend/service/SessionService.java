
package com.backend.service;

import com.backend.entity.AuthSession;
import com.backend.entity.User;
import com.backend.repository.AuthSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class SessionService {

    // Generate unpredictable session tokens using a cryptographically
    // secure random number generator.
    private final SecureRandom secureRandom = new SecureRandom();

    private final AuthSessionRepository sessionRepository;

    public SessionService(AuthSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public CreatedSession createSession(User user) {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        // This raw token is returned once to the client.
        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setTokenHash(hashToken(rawToken));

        // Sessions expire 30 minutes after creation.
        session.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        session.setRevoked(false);

        sessionRepository.save(session);

        return new CreatedSession(rawToken, session.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public AuthSession validateSession(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }

        String tokenHash = hashToken(rawToken);

        return sessionRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .filter(session -> session.getExpiresAt().isAfter(Instant.now()))
                .filter(session -> "ACTIVE".equalsIgnoreCase(
                        session.getUser().getStatus()))
                .orElse(null);
    }

    @Transactional
    public boolean revokeSession(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return false;
        }

        return sessionRepository
                .findByTokenHashAndRevokedFalse(hashToken(rawToken))
                .map(session -> {
                    session.setRevoked(true);
                    sessionRepository.save(session);
                    return true;
                })
                .orElse(false);
    }

    // Hash tokens before storing or looking them up in the database.
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            // SHA-256 is required by the Java runtime.
            throw new IllegalStateException("SHA-256 is unavailable.", ex);
        }
    }

    // Return the raw token only at creation time; never store it in the DB.
    public record CreatedSession(String token, Instant expiresAt) {
    }
    
@Transactional
public int revokeAllSessionsForUser(Long userId) {
    return sessionRepository.revokeAllActiveSessionsByUserId(userId);
}

}
