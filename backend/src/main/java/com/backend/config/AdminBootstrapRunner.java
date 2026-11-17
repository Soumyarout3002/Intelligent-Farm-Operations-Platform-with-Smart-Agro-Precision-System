
package com.backend.config;

import com.backend.entity.User;
import com.backend.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrapRunner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(user ->
                        "ADMIN".equalsIgnoreCase(user.getRole()));

        if (adminExists) {
            System.out.println(
                    "ADMIN account already exists. Bootstrap skipped.");
            return;
        }

        String name = System.getenv("ADMIN_BOOTSTRAP_NAME");
        String email = System.getenv("ADMIN_BOOTSTRAP_EMAIL");
        String password = System.getenv("ADMIN_BOOTSTRAP_PASSWORD");

        if (isBlank(name) || isBlank(email)
                || isBlank(password) || password.length() < 8) {
            throw new IllegalStateException(
                    "No ADMIN exists. Set ADMIN_BOOTSTRAP_NAME, "
                    + "ADMIN_BOOTSTRAP_EMAIL and "
                    + "ADMIN_BOOTSTRAP_PASSWORD (minimum 8 characters) "
                    + "before starting the backend."
            );
        }

        String normalizedEmail = email.trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException(
                    "The bootstrap email already belongs to another user."
            );
        }

        User admin = new User();
        admin.setName(name.trim());
        admin.setEmail(normalizedEmail);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole("ADMIN");
        admin.setStatus("ACTIVE");
        admin.setLanguage("English");

        userRepository.save(admin);

        System.out.println(
                "Initial ADMIN account created successfully: "
                + normalizedEmail);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
