package com.backend.controller;

import com.backend.dto.farm.PlotRequest;
import com.backend.dto.farm.PlotResponse;
import com.backend.entity.User;
import com.backend.service.PlotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/plots")
@RequiredArgsConstructor
public class PlotController {

    private final PlotService plotService;

    @PostMapping
    public ResponseEntity<PlotResponse> createPlot(
            @PathVariable Long farmId,
            @Valid @RequestBody PlotRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plotService.createPlot(
                        farmId, request, user.getId()));
    }

    @GetMapping
    public ResponseEntity<List<PlotResponse>> getFarmPlots(
            @PathVariable Long farmId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                plotService.getFarmPlots(farmId, user.getId()));
    }

    @GetMapping("/{plotId}")
    public ResponseEntity<PlotResponse> getPlot(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                plotService.getPlot(farmId, plotId, user.getId()));
    }

    @PutMapping("/{plotId}")
    public ResponseEntity<PlotResponse> updatePlot(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @Valid @RequestBody PlotRequest plotRequest,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        return ResponseEntity.ok(
                plotService.updatePlot(
                        farmId, plotId, plotRequest, user.getId()));
    }

    @DeleteMapping("/{plotId}")
    public ResponseEntity<Void> deletePlot(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            HttpServletRequest request) {

        User user = getAuthenticatedUser(request);

        plotService.deletePlot(farmId, plotId, user.getId());

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
