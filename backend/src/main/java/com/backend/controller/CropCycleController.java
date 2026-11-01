package com.backend.controller;

import com.backend.dto.crop.CropCycleRequest;
import com.backend.dto.crop.CropCycleResponse;
import com.backend.entity.User;
import com.backend.service.CropCycleService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/plots/{plotId}/crop-cycles")
public class CropCycleController {

    private final CropCycleService cropCycleService;

    public CropCycleController(CropCycleService cropCycleService) {
        this.cropCycleService = cropCycleService;
    }

    // Create crop cycle
    @PostMapping
    public ResponseEntity<CropCycleResponse> createCropCycle(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @Valid @RequestBody CropCycleRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cropCycleService.createCropCycle(
                        farmId, plotId, user.getId(), request
                ));
    }

    // Get all crop cycles
    @GetMapping
    public ResponseEntity<List<CropCycleResponse>> getCropCycles(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                cropCycleService.getCropCycles(
                        farmId, plotId, user.getId()
                )
        );
    }

    // Get one crop cycle
    @GetMapping("/{cycleId}")
    public ResponseEntity<CropCycleResponse> getCropCycle(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                cropCycleService.getCropCycle(
                        farmId, plotId, cycleId, user.getId()
                )
        );
    }

    // Update crop cycle
    @PutMapping("/{cycleId}")
    public ResponseEntity<CropCycleResponse> updateCropCycle(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @Valid @RequestBody CropCycleRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                cropCycleService.updateCropCycle(
                        farmId, plotId, cycleId, user.getId(), request
                )
        );
    }

    // Delete crop cycle
    @DeleteMapping("/{cycleId}")
    public ResponseEntity<Void> deleteCropCycle(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        cropCycleService.deleteCropCycle(
                farmId, plotId, cycleId, user.getId()
        );

        return ResponseEntity.noContent().build();
    }
}
