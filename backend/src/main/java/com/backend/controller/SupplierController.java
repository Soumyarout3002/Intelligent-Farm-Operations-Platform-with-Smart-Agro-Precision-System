package com.backend.controller;

import com.backend.dto.inventory.SupplierRequest;
import com.backend.dto.inventory.SupplierResponse;
import com.backend.entity.User;
import com.backend.service.SupplierService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/inventory/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @PathVariable Long farmId,
            @Valid @RequestBody SupplierRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(
                        farmId, user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getSuppliers(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                supplierService.getSuppliers(farmId, user.getId()));
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> getSupplier(
            @PathVariable Long farmId,
            @PathVariable Long supplierId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                supplierService.getSupplier(
                        farmId, supplierId, user.getId()));
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long farmId,
            @PathVariable Long supplierId,
            @Valid @RequestBody SupplierRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                supplierService.updateSupplier(
                        farmId, supplierId, user.getId(), request));
    }

    @DeleteMapping("/{supplierId}")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable Long farmId,
            @PathVariable Long supplierId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        supplierService.deleteSupplier(
                farmId, supplierId, user.getId());

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