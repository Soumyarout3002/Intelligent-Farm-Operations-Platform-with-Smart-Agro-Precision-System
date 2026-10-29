
package com.backend.controller;

import com.backend.dto.LoginRequest;
import com.backend.dto.RegisterRequest;
import com.backend.entity.User;
import com.backend.service.AuthService;
import com.backend.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(
            AuthService authService,
            SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        try {
            User user = authService.register(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Registration successful",
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "role", user.getRole()
                    )
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", ex.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        try {
            // Verify the user's email, password and account status first.
            User user = authService.login(request);

            // Create a server-side session only after successful verification.
            SessionService.CreatedSession session =
                    sessionService.createSession(user);

            // Return the raw token once; the database stores only its hash.
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Login successful",
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "role", user.getRole(),
                            "token", session.token(),
                            "expiresAt", session.expiresAt().toString()
                    )
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", ex.getMessage())
            );
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        // Accept a bearer token in the standard Authorization header.
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Bearer token is required.")
            );
        }

        String token = authorizationHeader.substring(7).trim();

        // Revoke the session so that the same token cannot be reused.
        boolean revoked = sessionService.revokeSession(token);

        if (!revoked) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Session is invalid or already logged out.")
            );
        }

        return ResponseEntity.ok(
                Map.of("message", "Logout successful")
        );
    }
}
