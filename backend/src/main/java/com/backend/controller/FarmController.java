package com.backend.controller;

import com.backend.dto.farm.FarmRequest;
import com.backend.dto.farm.FarmResponse;
import com.backend.entity.User;
import com.backend.service.FarmService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;
    private final com.backend.service.ActivityLogService activityLogService;

    @PostMapping
    public ResponseEntity<FarmResponse> createFarm(
            @Valid @RequestBody FarmRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        FarmResponse created = farmService.createFarm(request, user.getId());
        activityLogService.logActivity(user, "FARM_CREATED", "FARM", created.getId().toString(),
                "Farm created: " + created.getName(), null,
                java.util.Map.of("name", created.getName(), "location", created.getLocation()), httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FarmResponse>> getMyFarms(
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                farmService.getMyFarms(user.getId(), user.getRole()));
    }

    @GetMapping("/{farmId}")
    public ResponseEntity<FarmResponse> getFarm(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                farmService.getFarmById(farmId, user.getId(), user.getRole()));
    }

    @PutMapping("/{farmId}")
    public ResponseEntity<FarmResponse> updateFarm(
            @PathVariable Long farmId,
            @Valid @RequestBody FarmRequest farmRequest,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        FarmResponse updated = farmService.updateFarm(farmId, farmRequest, user.getId(), user.getRole());
        activityLogService.logActivity(user, "FARM_UPDATED", "FARM", updated.getId().toString(),
                "Farm updated: " + updated.getName(), null,
                java.util.Map.of("name", updated.getName(), "location", updated.getLocation()), request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{farmId}")
    public ResponseEntity<Void> deleteFarm(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        FarmResponse existing = farmService.getFarmById(farmId, user.getId(), user.getRole());
        farmService.deleteFarm(farmId, user.getId(), user.getRole());
        activityLogService.logActivity(user, "FARM_DELETED", "FARM", farmId.toString(),
                "Farm deleted: " + existing.getName(), null, null, request);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return user;
    }
}