
package com.backend.controller;

import com.backend.dto.inventory.InventoryOpeningValuationRequest;
import com.backend.dto.inventory.InventoryOpeningValuationResponse;
import com.backend.entity.User;
import com.backend.service.InventoryOpeningValuationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farms/{farmId}/inventory/items/{itemId}/warehouses/{warehouseId}/opening-valuation")
@RequiredArgsConstructor
public class InventoryOpeningValuationController {

    private final InventoryOpeningValuationService
            openingValuationService;

    @PostMapping
    public ResponseEntity<InventoryOpeningValuationResponse>
    createOpeningValuation(
            @PathVariable Long farmId,
            @PathVariable Long itemId,
            @PathVariable Long warehouseId,
            @Valid @RequestBody InventoryOpeningValuationRequest request,
            HttpServletRequest httpRequest) {

        Object value = httpRequest.getAttribute("authenticatedUser");

        if (!(value instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        InventoryOpeningValuationResponse response =
                openingValuationService.createOpeningValuation(
                        farmId,
                        itemId,
                        warehouseId,
                        user.getId(),
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }
}
