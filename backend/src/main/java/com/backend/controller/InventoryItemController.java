
package com.backend.controller;

import com.backend.dto.inventory.InventoryItemRequest;
import com.backend.dto.inventory.InventoryItemResponse;
import com.backend.entity.User;
import com.backend.service.InventoryItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/inventory/items")
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryItemService inventoryItemService;

    @PostMapping
    public ResponseEntity<InventoryItemResponse> createItem(
            @PathVariable Long farmId,
            @Valid @RequestBody InventoryItemRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryItemService.createItem(
                        farmId, request, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<InventoryItemResponse>> getFarmItems(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                inventoryItemService.getFarmItems(
                        farmId, user.getId()));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponse> getItem(
            @PathVariable Long farmId,
            @PathVariable Long itemId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                inventoryItemService.getItemById(
                        farmId, itemId, user.getId()));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<InventoryItemResponse> updateItem(
            @PathVariable Long farmId,
            @PathVariable Long itemId,
            @Valid @RequestBody InventoryItemRequest itemRequest,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                inventoryItemService.updateItem(
                        farmId, itemId, itemRequest, user.getId()));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long farmId,
            @PathVariable Long itemId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        inventoryItemService.deleteItem(
                farmId, itemId, user.getId());

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
