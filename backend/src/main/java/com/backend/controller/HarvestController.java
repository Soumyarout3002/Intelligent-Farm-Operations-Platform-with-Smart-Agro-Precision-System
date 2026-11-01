package com.backend.controller;

import com.backend.dto.crop.HarvestRequest;
import com.backend.dto.crop.HarvestResponse;
import com.backend.entity.User;
import com.backend.service.HarvestService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/farms/{farmId}/plots/{plotId}/crop-cycles/{cycleId}/harvests"
)
public class HarvestController {

    private final HarvestService harvestService;

    public HarvestController(HarvestService harvestService) {
        this.harvestService = harvestService;
    }

    @PostMapping
    public ResponseEntity<HarvestResponse> createHarvest(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @Valid @RequestBody HarvestRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestService.createHarvest(
                        farmId, plotId, cycleId, user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<HarvestResponse>> getHarvests(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                harvestService.getHarvests(
                        farmId, plotId, cycleId, user.getId()));
    }

    @GetMapping("/{harvestId}")
    public ResponseEntity<HarvestResponse> getHarvest(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long harvestId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                harvestService.getHarvest(
                        farmId, plotId, cycleId,
                        harvestId, user.getId()));
    }

    @PutMapping("/{harvestId}")
    public ResponseEntity<HarvestResponse> updateHarvest(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long harvestId,
            @Valid @RequestBody HarvestRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                harvestService.updateHarvest(
                        farmId, plotId, cycleId,
                        harvestId, user.getId(), request));
    }

    @DeleteMapping("/{harvestId}")
    public ResponseEntity<Void> deleteHarvest(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long harvestId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        harvestService.deleteHarvest(
                farmId, plotId, cycleId,
                harvestId, user.getId());

        return ResponseEntity.noContent().build();
    }
}