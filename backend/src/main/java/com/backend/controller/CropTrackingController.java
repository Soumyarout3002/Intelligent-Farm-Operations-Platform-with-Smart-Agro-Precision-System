package com.backend.controller;

import com.backend.dto.crop.*;
import com.backend.entity.User;
import com.backend.service.CropTrackingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/farms/{farmId}/plots/{plotId}/crop-cycles/{cycleId}"
)
public class CropTrackingController {

    private final CropTrackingService trackingService;

    public CropTrackingController(CropTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    // ---------------- GROWTH STAGES ----------------

    @PostMapping("/growth-stages")
    public ResponseEntity<GrowthStageResponse> addGrowthStage(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @Valid @RequestBody GrowthStageRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trackingService.addGrowthStage(
                        farmId, plotId, cycleId, user.getId(), request));
    }

    @GetMapping("/growth-stages")
    public ResponseEntity<List<GrowthStageResponse>> getGrowthStages(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(trackingService.getGrowthStages(
                farmId, plotId, cycleId, user.getId()));
    }

    @PutMapping("/growth-stages/{stageId}")
    public ResponseEntity<GrowthStageResponse> updateGrowthStage(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long stageId,
            @Valid @RequestBody GrowthStageRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(trackingService.updateGrowthStage(
                farmId, plotId, cycleId, stageId, user.getId(), request));
    }

    @DeleteMapping("/growth-stages/{stageId}")
    public ResponseEntity<Void> deleteGrowthStage(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long stageId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        trackingService.deleteGrowthStage(
                farmId, plotId, cycleId, stageId, user.getId());

        return ResponseEntity.noContent().build();
    }

    // ---------------- FARMING ACTIVITIES ----------------

    @PostMapping("/activities")
    public ResponseEntity<CropActivityResponse> addActivity(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @Valid @RequestBody CropActivityRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trackingService.addActivity(
                        farmId, plotId, cycleId, user.getId(), request));
    }

    @GetMapping("/activities")
    public ResponseEntity<List<CropActivityResponse>> getActivities(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(trackingService.getActivities(
                farmId, plotId, cycleId, user.getId()));
    }

    @PutMapping("/activities/{activityId}")
    public ResponseEntity<CropActivityResponse> updateActivity(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long activityId,
            @Valid @RequestBody CropActivityRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(trackingService.updateActivity(
                farmId, plotId, cycleId, activityId, user.getId(), request));
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long activityId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        trackingService.deleteActivity(
                farmId, plotId, cycleId, activityId, user.getId());

        return ResponseEntity.noContent().build();
    }
}
