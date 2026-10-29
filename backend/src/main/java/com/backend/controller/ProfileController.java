
package com.backend.controller;

import com.backend.entity.User;
import com.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Read the profile of the user authenticated by AuthInterceptor.
    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        User user = getAuthenticatedUser(request);

        if (user == null) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Authentication is required.")
            );
        }

        return ResponseEntity.ok(toProfileResponse(user));
    }

    // Accept only fields the user is allowed to update from their profile.
    public record UpdateProfileRequest(
            @NotBlank(message = "Name is required.")
            @Size(max = 100, message = "Name must be at most 100 characters.")
            String name,

            @Pattern(
                    regexp = "^$|^[0-9]{10,15}$",
                    message = "Mobile must contain 10 to 15 digits."
            )
            String mobile
    ) {}

    // Update the authenticated user's profile; never accept a user ID from the client.
    @PutMapping
    @Transactional
    public ResponseEntity<?> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody UpdateProfileRequest body) {

        User user = getAuthenticatedUser(request);

        if (user == null) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "Authentication is required.")
            );
        }

        // Normalize and validate the name before saving it.
        String name = body.name().trim();
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Name is required.")
            );
        }

        user.setName(name);
        user.setMobile(
                body.mobile() == null || body.mobile().isBlank()
                        ? null
                        : body.mobile().trim()
        );

        // JPA persists the updated managed entity when the transaction completes.
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(toProfileResponse(savedUser));
    }

    // The interceptor must set this request attribute after validating the bearer token.
    private User getAuthenticatedUser(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");
        return value instanceof User ? (User) value : null;
    }

    // Return only public profile fields; never expose passwordHash.
    private Map<String, Object> toProfileResponse(User user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("role", user.getRole());
        result.put("status", user.getStatus());
        result.put("mobile", user.getMobile() == null ? "" : user.getMobile());
        result.put("language", user.getLanguage() == null ? "English" : user.getLanguage());
        result.put("createdAt", user.getCreatedAt() == null
                ? ""
                : user.getCreatedAt().toString());
        return result;
    }
}
