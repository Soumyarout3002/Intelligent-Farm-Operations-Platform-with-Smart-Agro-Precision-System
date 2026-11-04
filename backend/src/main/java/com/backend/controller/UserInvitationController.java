package com.backend.controller;

import com.backend.dto.AcceptInvitationRequest;
import com.backend.dto.InvitationRequest;
import com.backend.entity.User;
import com.backend.service.InvitationService;
import com.backend.security.RequireRole;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserInvitationController {

    private final InvitationService invitationService;

    public UserInvitationController(
            InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    // Admin sends an invitation email.
    @PostMapping("/admin/invitations")
    @RequireRole({"ADMIN"})
    public ResponseEntity<?> sendInvitation(
            @Valid @RequestBody InvitationRequest request,
            HttpServletRequest httpRequest) {

        try {
            User admin = (User) httpRequest.getAttribute(
                    "authenticatedUser");

            Map<String, Object> response =
                    invitationService.createInvitation(
                            request, admin.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", ex.getMessage()));

        } catch (IllegalStateException ex) {
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE).body(
                    Map.of("message", ex.getMessage()));
        }
    }

    // Validate invitation token.
    @GetMapping("/invitations/validate")
    public ResponseEntity<?> validateInvitation(
            @RequestParam String token) {

        try {
            return ResponseEntity.ok(
                    invitationService.validateInvitation(token));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", ex.getMessage()));
        }
    }

    // Accept invitation and create account.
    @PostMapping("/invitations/accept")
    public ResponseEntity<?> acceptInvitation(
            @Valid @RequestBody AcceptInvitationRequest request) {

        try {
            User user = invitationService.acceptInvitation(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Invitation accepted successfully.",
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "role", user.getRole()
                    )
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", ex.getMessage()));
        }
    }
}