
package com.backend.service;

import com.backend.entity.User;
import com.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {

    @Bean
    public CommandLineRunner createDemoAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String email = "admin@farmverse.demo";

            // Do not create a duplicate Admin.
            if (userRepository.existsByEmailIgnoreCase(email)) {
                System.out.println("Demo Admin already exists.");
                return;
            }

            User admin = new User();

            admin.setName("FarmVerse Admin");
            admin.setEmail(email);
            admin.setPasswordHash(
                    passwordEncoder.encode("Admin@123"));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            admin.setLanguage("English");

            userRepository.save(admin);

            System.out.println("Demo Admin created successfully.");
        };
    }
}
