
package com.backend.service;

import com.backend.dto.RegisterRequest;
import com.backend.entity.User;
import com.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.backend.dto.LoginRequest;

import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> PUBLIC_ROLES = Set.of("FARMER");

    @Value("${app.admin.setup-secret:}")
    private String adminSetupSecret;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.backend.repository.InvitationRepository invitationRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            com.backend.repository.InvitationRepository invitationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String role = request.getRole().trim().toUpperCase(Locale.ROOT);
        if (!PUBLIC_ROLES.contains(role)) {
            throw new IllegalArgumentException("Field Worker and Admin accounts require an invitation or first-Admin setup.");
        }
        return createUser(request, role);
    }

    @Transactional
    public User createFirstAdmin(RegisterRequest request) {
        if (adminSetupSecret == null || adminSetupSecret.isBlank()
                || request.getAdminSetupSecret() == null
                || !java.security.MessageDigest.isEqual(
                    adminSetupSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    request.getAdminSetupSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Admin setup secret is incorrect or not configured.");
        }
        if (userRepository.existsByRoleIgnoreCase("ADMIN")) {
            throw new IllegalArgumentException("The first Admin account has already been created.");
        }
        return createUser(request, "ADMIN");
    }

    @Transactional
    public User registerByInvitation(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        com.backend.entity.Invitation invite = invitationRepository.findByTokenHash(hashToken(request.getInvitationToken()))
                .orElseThrow(() -> new IllegalArgumentException("Invitation is invalid or expired."));
        if (invite.getUsedAt() != null || invite.getExpiresAt().isBefore(java.time.LocalDateTime.now())
                || !invite.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Invitation is invalid, expired, or belongs to another email.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already registered.");
        }
        invite.setUsedAt(java.time.LocalDateTime.now());
        invitationRepository.save(invite);
        return createUser(request, invite.getRole());
    }

    @Transactional
    public User createByAdmin(RegisterRequest request) {
        String role = request.getRole().trim().toUpperCase(Locale.ROOT);
        if (!"FARMER".equals(role)) {
            throw new IllegalArgumentException("Admin and Field Worker accounts must be created through invitations.");
        }
        return createUser(request, role);
    }

    private User createUser(RegisterRequest request, String role) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("This email is already registered.");
        }
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setMobile(request.getMobile());
        user.setLanguage(request.getLanguage() == null || request.getLanguage().isBlank()
                ? "English" : request.getLanguage().trim());
        return userRepository.save(user);
    }

    private String hashToken(String token) {
        try {
            byte[] bytes = java.security.MessageDigest.getInstance("SHA-256")
                    .digest((token == null ? "" : token).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @Transactional(readOnly = true)
    public User login(LoginRequest request) {
        String email = request.getEmail().trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password."));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Invalid email or password.");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException(
                    "Your account is inactive.");
        }

        return user;
    }

}
