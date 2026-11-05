
package com.backend.controller;

import com.backend.dto.inventory.WarehouseRequest;
import com.backend.dto.inventory.WarehouseResponse;
import com.backend.entity.User;
import com.backend.service.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/inventory/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @PathVariable Long farmId,
            @Valid @RequestBody WarehouseRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(warehouseService.createWarehouse(
                        farmId, request, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<WarehouseResponse>> getFarmWarehouses(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                warehouseService.getFarmWarehouses(
                        farmId, user.getId()));
    }

    @GetMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponse> getWarehouse(
            @PathVariable Long farmId,
            @PathVariable Long warehouseId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                warehouseService.getWarehouseById(
                        farmId, warehouseId, user.getId()));
    }

    @PutMapping("/{warehouseId}")
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Long farmId,
            @PathVariable Long warehouseId,
            @Valid @RequestBody WarehouseRequest warehouseRequest,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                warehouseService.updateWarehouse(
                        farmId, warehouseId,
                        warehouseRequest, user.getId()));
    }

    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<Void> deleteWarehouse(
            @PathVariable Long farmId,
            @PathVariable Long warehouseId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        warehouseService.deleteWarehouse(
                farmId, warehouseId, user.getId());

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
