
package com.backend.controller;

import com.backend.dto.equipment.EquipmentUsageRequest;
import com.backend.dto.equipment.EquipmentUsageResponse;
import com.backend.entity.User;
import com.backend.service.EquipmentUsageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/equipment")
@RequiredArgsConstructor
public class EquipmentUsageController {

    private final EquipmentUsageService usageService;

    @PostMapping("/usage")
    public ResponseEntity<EquipmentUsageResponse> recordUsage(
            @PathVariable Long farmId,
            @Valid @RequestBody EquipmentUsageRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        EquipmentUsageResponse response =
                usageService.recordUsage(farmId, request, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/usage")
    public ResponseEntity<List<EquipmentUsageResponse>> getFarmUsage(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                usageService.getFarmUsage(farmId, user.getId())
        );
    }

    @GetMapping("/{equipmentId}/usage")
    public ResponseEntity<List<EquipmentUsageResponse>> getEquipmentUsage(
            @PathVariable Long farmId,
            @PathVariable Long equipmentId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                usageService.getEquipmentUsage(
                        farmId, equipmentId, user.getId())
        );
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
