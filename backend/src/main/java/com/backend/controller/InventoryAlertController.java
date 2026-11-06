
package com.backend.controller;

import com.backend.dto.inventory.InventoryAlertResponse;
import com.backend.entity.User;
import com.backend.service.InventoryAlertService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/inventory/alerts")
@RequiredArgsConstructor
public class InventoryAlertController {

    private final InventoryAlertService inventoryAlertService;

    @GetMapping
    public ResponseEntity<List<InventoryAlertResponse>> getFarmAlerts(
            @PathVariable Long farmId,
            @RequestParam(defaultValue = "30") int expiryDays,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        List<InventoryAlertResponse> alerts =
                inventoryAlertService.getFarmAlerts(
                        farmId,
                        user.getId(),
                        expiryDays
                );

        return ResponseEntity.ok(alerts);
    }

    private User getAuthenticatedUser(HttpServletRequest request) {

        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        return user;
    }
}
