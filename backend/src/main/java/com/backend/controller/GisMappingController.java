package com.backend.controller;

import com.backend.dto.gis.PlotBoundaryRequest;
import com.backend.dto.gis.PlotBoundaryResponse;
import com.backend.entity.User;
import com.backend.service.GisMappingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/farms/{farmId}/plots/{plotId}/boundary")
@RequiredArgsConstructor
public class GisMappingController {

    private final GisMappingService gisMappingService;

    private Long getOwnerId(HttpServletRequest request) {

        Object authenticatedUser =
                request.getAttribute("authenticatedUser");

        if (!(authenticatedUser instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        return user.getId();
    }

    @PostMapping
    public ResponseEntity<PlotBoundaryResponse> saveBoundary(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @Valid @RequestBody PlotBoundaryRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gisMappingService.saveBoundary(
                        farmId,
                        plotId,
                        getOwnerId(httpRequest),
                        request));
    }

    @GetMapping
    public ResponseEntity<PlotBoundaryResponse> getBoundary(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                gisMappingService.getBoundary(
                        farmId,
                        plotId,
                        getOwnerId(httpRequest)));
    }

    @PutMapping
    public ResponseEntity<PlotBoundaryResponse> updateBoundary(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @Valid @RequestBody PlotBoundaryRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                gisMappingService.updateBoundary(
                        farmId,
                        plotId,
                        getOwnerId(httpRequest),
                        request));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBoundary(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            HttpServletRequest httpRequest) {

        gisMappingService.deleteBoundary(
                farmId,
                plotId,
                getOwnerId(httpRequest));

        return ResponseEntity.noContent().build();
    }
}
