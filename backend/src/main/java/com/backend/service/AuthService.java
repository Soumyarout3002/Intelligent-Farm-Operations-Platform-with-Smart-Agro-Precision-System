
package com.backend.service;

import com.backend.dto.RegisterRequest;
import com.backend.entity.User;
import com.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.backend.dto.LoginRequest;

import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private static final Set<String> PUBLIC_ROLES =
            Set.of("FARMER", "FIELD_WORKER");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = request.getEmail().trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "This email is already registered.");
        }

        String role = request.getRole().trim()
                .toUpperCase(Locale.ROOT);

        if (!PUBLIC_ROLES.contains(role)) {
            throw new IllegalArgumentException(
                    "This role cannot be selected during public registration.");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus("ACTIVE");
        user.setMobile(request.getMobile());
        user.setLanguage(
                request.getLanguage() == null
                        || request.getLanguage().isBlank()
                        ? "English"
                        : request.getLanguage().trim());

        return userRepository.save(user);
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
