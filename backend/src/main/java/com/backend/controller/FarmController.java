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

    @PostMapping
    public ResponseEntity<FarmResponse> createFarm(
            @Valid @RequestBody FarmRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(farmService.createFarm(request, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<FarmResponse>> getMyFarms(
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                farmService.getMyFarms(user.getId()));
    }

    @GetMapping("/{farmId}")
    public ResponseEntity<FarmResponse> getFarm(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                farmService.getFarmById(farmId, user.getId()));
    }

    @PutMapping("/{farmId}")
    public ResponseEntity<FarmResponse> updateFarm(
            @PathVariable Long farmId,
            @Valid @RequestBody FarmRequest farmRequest,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                farmService.updateFarm(
                        farmId, farmRequest, user.getId()));
    }

    @DeleteMapping("/{farmId}")
    public ResponseEntity<Void> deleteFarm(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        farmService.deleteFarm(farmId, user.getId());

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