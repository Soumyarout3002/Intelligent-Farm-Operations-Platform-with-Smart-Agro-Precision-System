package com.backend.controller;

import com.backend.dto.equipment.EquipmentRequest;
import com.backend.dto.equipment.EquipmentResponse;
import com.backend.entity.User;
import com.backend.service.EquipmentService;
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
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<EquipmentResponse> createEquipment(
            @PathVariable Long farmId,
            @Valid @RequestBody EquipmentRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentService.createEquipment(
                        farmId, request, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<EquipmentResponse>> getFarmEquipment(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                equipmentService.getFarmEquipment(
                        farmId, user.getId()));
    }

    @GetMapping("/{equipmentId}")
    public ResponseEntity<EquipmentResponse> getEquipment(
            @PathVariable Long farmId,
            @PathVariable Long equipmentId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                equipmentService.getEquipmentById(
                        farmId, equipmentId, user.getId()));
    }

    @PutMapping("/{equipmentId}")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable Long farmId,
            @PathVariable Long equipmentId,
            @Valid @RequestBody EquipmentRequest equipmentRequest,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                equipmentService.updateEquipment(
                        farmId, equipmentId,
                        equipmentRequest, user.getId()));
    }

    @DeleteMapping("/{equipmentId}")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable Long farmId,
            @PathVariable Long equipmentId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        equipmentService.deleteEquipment(
                farmId, equipmentId, user.getId());

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