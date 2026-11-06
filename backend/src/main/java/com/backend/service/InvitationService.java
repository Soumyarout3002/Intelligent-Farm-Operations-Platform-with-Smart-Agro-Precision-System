package com.backend.service;

import com.backend.dto.AcceptInvitationRequest;
import com.backend.dto.InvitationRequest;
import com.backend.entity.User;
import com.backend.entity.UserInvitation;
import com.backend.repository.UserInvitationRepository;
import com.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class InvitationService {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN","FARMER", "FIELD_WORKER");

    private final UserInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public InvitationService(
            UserInvitationRepository invitationRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public Map<String, Object> createInvitation(
            InvitationRequest request,
            Long invitedByUserId) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String role = request.role()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!ALLOWED_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "Only FARMER and FIELD_WORKER roles can be invited.");
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "This email is already registered.");
        }

        // Keep previous pending invitations active until the new email succeeds.
        var previousInvitations =
                invitationRepository.findAllByEmailIgnoreCaseAndStatus(
                        email, "PENDING");

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        UserInvitation invitation = new UserInvitation();

        invitation.setEmail(email);
        invitation.setRole(role);
        invitation.setTokenHash(hashToken(rawToken));
        invitation.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS));
        invitation.setStatus("PENDING");
        invitation.setInvitedByUserId(invitedByUserId);

        // Save first so the token exists in the database before sending email.
        UserInvitation savedInvitation =
                invitationRepository.save(invitation);

        String invitationLink =
                frontendBaseUrl.replaceAll("/+$", "")
                + "/accept-invitation?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        try {
            emailService.sendInvitationEmail(
                    email, role, invitationLink);

        } catch (RuntimeException ex) {

            // Do not leave an invitation pending if email sending fails.
            savedInvitation.setStatus("CANCELLED");
            invitationRepository.save(savedInvitation);

            throw new IllegalStateException(
                    "Invitation could not be emailed. Please check Gmail SMTP configuration.",
                    ex);
        }

        // Cancel older invitations only after the new email was sent.
        previousInvitations.forEach(
                previous -> previous.setStatus("CANCELLED"));

        invitationRepository.saveAll(previousInvitations);

        return Map.of(
                "message", "Invitation email sent successfully.",
                "email", email,
                "role", role,
                "expiresAt", savedInvitation.getExpiresAt().toString()
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Object> validateInvitation(String rawToken) {

        UserInvitation invitation = getValidInvitation(rawToken);

        return Map.of(
                "email", invitation.getEmail(),
                "role", invitation.getRole(),
                "expiresAt", invitation.getExpiresAt().toString()
        );
    }

    @Transactional
    public User acceptInvitation(AcceptInvitationRequest request) {

        UserInvitation invitation =
                getValidInvitation(request.token());

        String email = invitation.getEmail();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists.");
        }

        User user = new User();

        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password()));
        user.setRole(invitation.getRole());
        user.setStatus("ACTIVE");
        user.setLanguage("English");

        User savedUser = userRepository.save(user);

        invitation.setStatus("ACCEPTED");
        invitation.setAcceptedAt(Instant.now());

        invitationRepository.save(invitation);

        return savedUser;
    }

    private UserInvitation getValidInvitation(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Invalid or expired invitation.");
        }

        UserInvitation invitation = invitationRepository
                .findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid or expired invitation."));

        if (!"PENDING".equals(invitation.getStatus())
                || !invitation.getExpiresAt().isAfter(Instant.now())) {

            throw new IllegalArgumentException(
                    "Invalid or expired invitation.");
        }

        return invitation;
    }

    private String hashToken(String rawToken) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    rawToken.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to hash invitation token.", ex);
        }
    }
}