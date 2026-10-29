
package com.backend.controller;

import com.backend.dto.RegisterRequest;
import com.backend.entity.User;
import com.backend.repository.UserRepository;
import com.backend.security.RequireRole;
import com.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.backend.repository.AuthSessionRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequireRole("ADMIN")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final AuthSessionRepository authSessionRepository;

    public AdminUserController(
        UserRepository userRepository,
        AuthService authService,
        AuthSessionRepository authSessionRepository) {

    this.userRepository = userRepository;
    this.authService = authService;
    this.authSessionRepository = authSessionRepository;
}

    // GET /api/admin/users - list all users.
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    // GET /api/admin/users/{id} - get one user.
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user ->
                        ResponseEntity.ok(toUserResponse(user)))
                .orElseGet(() -> error(
                        HttpStatus.NOT_FOUND, "User not found."));
    }

    // POST /api/admin/users - create a Farmer or Field Worker.
    // AuthService performs duplicate checks and BCrypt password hashing.
    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(toUserResponse(user));
        } catch (IllegalArgumentException ex) {
            return error(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    // Fields that Admin can update without changing a user's role.
    public record UpdateUserRequest(
            @NotBlank(message = "Name is required.")
            @Size(max = 100, message = "Name must be at most 100 characters.")
            String name,

            @NotBlank(message = "Email is required.")
            @Email(message = "Enter a valid email address.")
            @Size(max = 150, message = "Email must be at most 150 characters.")
            String email,

            @Pattern(
                    regexp = "^$|^[0-9]{10,15}$",
                    message = "Mobile must contain 10 to 15 digits."
            )
            String mobile,

            @Size(max = 30, message = "Language preference is too long.")
            String language
    ) {}

    // PUT /api/admin/users/{id} - update user details.
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return error(HttpStatus.NOT_FOUND, "User not found.");
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);

        boolean emailUsed = userRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(id))
                .isPresent();

        if (emailUsed) {
            return error(
                    HttpStatus.CONFLICT,
                    "This email is already registered.");
        }

        user.setName(request.name().trim());
        user.setEmail(email);
        user.setMobile(
                request.mobile() == null || request.mobile().isBlank()
                        ? null : request.mobile().trim());
        user.setLanguage(
                request.language() == null || request.language().isBlank()
                        ? "English" : request.language().trim());

        return ResponseEntity.ok(
                toUserResponse(userRepository.save(user)));
    }

    public record UpdateStatusRequest(
            @NotBlank(message = "Status is required.")
            String status
    ) {}

    // PATCH /api/admin/users/{id}/status - activate/deactivate a user.
    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request,
            HttpServletRequest httpRequest) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            return error(HttpStatus.NOT_FOUND, "User not found.");
        }

        String status = request.status().trim().toUpperCase(Locale.ROOT);

        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            return error(
                    HttpStatus.BAD_REQUEST,
                    "Status must be ACTIVE or INACTIVE.");
        }

        User currentUser = getAuthenticatedUser(httpRequest);

        if (currentUser != null && currentUser.getId().equals(id)) {
            return error(
                    HttpStatus.CONFLICT,
                    "You cannot change your own account status.");
        }

        // Never deactivate the last active Admin.
        if (user.getRole().equalsIgnoreCase("ADMIN")
                && status.equals("INACTIVE")
                && user.getStatus().equalsIgnoreCase("ACTIVE")) {

            long activeAdmins = userRepository.findAll().stream()
                    .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                    .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                    .count();

            if (activeAdmins <= 1) {
                return error(
                        HttpStatus.CONFLICT,
                        "At least one active Admin must remain.");
            }
        }

        user.setStatus(status);
        return ResponseEntity.ok(
                toUserResponse(userRepository.save(user)));
    }

    // DELETE /api/admin/users/{id} - delete a user.
    
@DeleteMapping("/{id}")
@Transactional
public ResponseEntity<?> deleteUser(
        @PathVariable Long id,
        HttpServletRequest httpRequest) {

    User user = userRepository.findById(id).orElse(null);

    if (user == null) {
        return error(HttpStatus.NOT_FOUND, "User not found.");
    }

    User currentUser = getAuthenticatedUser(httpRequest);

    // Prevent the logged-in Admin from deleting their own account.
    if (currentUser != null && currentUser.getId().equals(id)) {
        return error(
                HttpStatus.CONFLICT,
                "You cannot delete your own account.");
    }

    // Never delete the last Admin account.
    if ("ADMIN".equalsIgnoreCase(user.getRole())) {
        long adminCount = userRepository.findAll().stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .count();

        if (adminCount <= 1) {
            return error(
                    HttpStatus.CONFLICT,
                    "The last Admin cannot be deleted.");
        }
    }

    // Delete the user's sessions first to satisfy the foreign key.
    authSessionRepository.deleteAllByUser_Id(id);

    // Now the user can be deleted safely.
    userRepository.delete(user);

    return ResponseEntity.noContent().build();
}

    // AuthInterceptor sets this attribute after validating the token.
    private User getAuthenticatedUser(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");
        return value instanceof User ? (User) value : null;
    }

    // Only expose safe fields; never return passwordHash.
    private Map<String, Object> toUserResponse(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("status", user.getStatus());
        result.put("mobile", user.getMobile() == null ? "" : user.getMobile());
        result.put("language",
                user.getLanguage() == null ? "English" : user.getLanguage());
        result.put("createdAt", user.getCreatedAt() == null
                ? "" : user.getCreatedAt().toString());
        return result;
    }

    private ResponseEntity<Map<String, String>> error(
            HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message));
    }
}
