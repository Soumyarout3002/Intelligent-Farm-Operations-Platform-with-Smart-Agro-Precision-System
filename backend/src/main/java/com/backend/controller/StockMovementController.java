package com.backend.controller;

import com.backend.dto.inventory.StockMovementRequest;
import com.backend.dto.inventory.StockMovementResponse;
import com.backend.entity.User;
import com.backend.service.StockMovementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/inventory/items/{itemId}/stock")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping("/in")
    public ResponseEntity<StockMovementResponse> stockIn(
            @PathVariable Long farmId,
            @PathVariable Long itemId,
            @Valid @RequestBody StockMovementRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.stockIn(
                        farmId, itemId, user.getId(), request));
    }

    @PostMapping("/out")
    public ResponseEntity<StockMovementResponse> stockOut(
            @PathVariable Long farmId,
            @PathVariable Long itemId,
            @Valid @RequestBody StockMovementRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.stockOut(
                        farmId, itemId, user.getId(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<StockMovementResponse>> getItemHistory(
            @PathVariable Long farmId,
            @PathVariable Long itemId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                stockMovementService.getItemHistory(
                        farmId, itemId, user.getId()));
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