
package com.backend.controller;

import com.backend.dto.equipment.EquipmentBreakdownRequest;
import com.backend.dto.equipment.EquipmentBreakdownResponse;
import com.backend.entity.User;
import com.backend.service.EquipmentBreakdownService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farms/{farmId}/equipment/breakdowns")
@RequiredArgsConstructor
public class EquipmentBreakdownController {

    private final EquipmentBreakdownService breakdownService;

    @PostMapping
    public ResponseEntity<EquipmentBreakdownResponse> reportBreakdown(
            @PathVariable Long farmId,
            @Valid @RequestBody EquipmentBreakdownRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        EquipmentBreakdownResponse response =
                breakdownService.reportBreakdown(farmId, request, user);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EquipmentBreakdownResponse>> getBreakdowns(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                breakdownService.getBreakdowns(farmId, user)
        );
    }

    @PatchMapping("/{breakdownId}/status")
    public ResponseEntity<EquipmentBreakdownResponse> updateStatus(
            @PathVariable Long farmId,
            @PathVariable Long breakdownId,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        User admin = getAuthenticatedUser(httpRequest);

        String status = request.get("status");

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required");
        }

        return ResponseEntity.ok(
                breakdownService.updateBreakdownStatus(
                        farmId, breakdownId, status, admin)
        );
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required");
        }

        return user;
    }
}
