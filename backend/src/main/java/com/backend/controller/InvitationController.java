package com.backend.controller;

import com.backend.entity.User;
import com.backend.security.RequireRole;
import com.backend.service.InvitationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/invitations")
@RequireRole("ADMIN")
public class InvitationController {
    private final InvitationService invitationService;
    public InvitationController(InvitationService invitationService) { this.invitationService = invitationService; }

    public record InviteRequest(@NotBlank @Email String email, @NotBlank String role) {}

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody InviteRequest request, HttpServletRequest httpRequest) {
        Object value = httpRequest.getAttribute("authenticatedUser");
        if (!(value instanceof User admin)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message","Authentication required."));
        try {
            var invite = invitationService.create(request.email(), request.role(), admin.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", invite.id(), "email", invite.email(), "role", invite.role(),
                "token", invite.token(), "expiresAt", invite.expiresAt().toString()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
