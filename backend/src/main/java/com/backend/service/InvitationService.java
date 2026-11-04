package com.backend.service;

import com.backend.entity.Invitation;
import com.backend.repository.InvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class InvitationService {
    private final InvitationRepository repository;
    public InvitationService(InvitationRepository repository) { this.repository = repository; }

    @Transactional
    public CreatedInvite create(String email, String role, Long adminId) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!java.util.Set.of("ADMIN", "FARMER", "FIELD_WORKER").contains(normalizedRole))
            throw new IllegalArgumentException("Invalid invitation role.");
        String token = UUID.randomUUID().toString() + UUID.randomUUID();
        Invitation invite = Invitation.builder().email(email.trim().toLowerCase(Locale.ROOT))
            .role(normalizedRole).tokenHash(hash(token)).expiresAt(LocalDateTime.now().plusDays(7))
            .createdBy(adminId).build();
        repository.save(invite);
        return new CreatedInvite(invite.getId(), invite.getEmail(), invite.getRole(), token, invite.getExpiresAt());
    }

    @Transactional
    public Invitation consume(String token, String email) {
        Invitation invite = repository.findByTokenHash(hash(token == null ? "" : token))
            .orElseThrow(() -> new IllegalArgumentException("Invitation is invalid or expired."));
        if (invite.getUsedAt() != null || invite.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Invitation is invalid or expired.");
        if (!invite.getEmail().equalsIgnoreCase(email == null ? "" : email.trim()))
            throw new IllegalArgumentException("This invitation belongs to a different email address.");
        invite.setUsedAt(LocalDateTime.now());
        return repository.save(invite);
    }

    private String hash(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
    public record CreatedInvite(Long id, String email, String role, String token, LocalDateTime expiresAt) {}
}
