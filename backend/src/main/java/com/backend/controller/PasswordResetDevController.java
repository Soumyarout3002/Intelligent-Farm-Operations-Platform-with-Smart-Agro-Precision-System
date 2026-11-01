
package com.backend.controller;

import com.backend.dto.ForgotPasswordRequest;
import com.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dev/password-reset")
@Profile("dev")
public class PasswordResetDevController {

    private final PasswordResetService passwordResetService;

    public PasswordResetDevController(
            PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> generateTestToken(
            @Valid @RequestBody ForgotPasswordRequest request) {

        String token =
                passwordResetService.createResetToken(request.email());

        return ResponseEntity.ok(
                Map.of("token", token)
        );
    }
    @PostMapping("/link")
public ResponseEntity<?> generateResetLink(
        @Valid @RequestBody ForgotPasswordRequest request) {

    String token = passwordResetService.createResetToken(request.email());

    String resetLink = "http://localhost:5173/reset-password?token="
            + java.net.URLEncoder.encode(
                token,
                java.nio.charset.StandardCharsets.UTF_8
            );

    return ResponseEntity.ok(Map.of(
            "message", "For local testing, use the link below to reset your password.",
            "resetLink", resetLink
    ));
}
}
