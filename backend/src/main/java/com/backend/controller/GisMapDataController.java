package com.backend.controller;

import com.backend.dto.gis.FarmMapResponse;
import com.backend.dto.gis.PlotMapResponse;
import com.backend.entity.User;
import com.backend.service.GisMapDataService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/gis")
@RequiredArgsConstructor
public class GisMapDataController {

    private final GisMapDataService gisMapDataService;

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

    @GetMapping("/farms")
    public ResponseEntity<List<FarmMapResponse>> getMyFarmsMapData(
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                gisMapDataService.getMyFarmsMapData(
                        getOwnerId(httpRequest)));
    }

    @GetMapping("/farms/{farmId}")
    public ResponseEntity<FarmMapResponse> getFarmMapData(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                gisMapDataService.getFarmMapData(
                        farmId, getOwnerId(httpRequest)));
    }

    @GetMapping("/farms/filter")
public ResponseEntity<List<FarmMapResponse>> filterMyFarms(
        HttpServletRequest request,
        @RequestParam(required = false) String farmName,
        @RequestParam(required = false) String farmStatus,
        @RequestParam(required = false) String crop,
        @RequestParam(required = false) String plotName,
        @RequestParam(required = false) String plotStatus) {

    User user = (User) request.getAttribute("authenticatedUser");

    return ResponseEntity.ok(
            gisMapDataService.filterMyFarms(
                    user.getId(),
                    farmName,
                    farmStatus,
                    crop,
                    plotName,
                    plotStatus
            )
    );
}

@GetMapping("/farms/{farmId}/plots/filter")
public ResponseEntity<List<PlotMapResponse>> filterPlots(
        @PathVariable Long farmId,
        HttpServletRequest request,
        @RequestParam(required = false) String plotName,
        @RequestParam(required = false) String plotStatus,
        @RequestParam(required = false) String crop) {

    User user = (User) request.getAttribute("authenticatedUser");

    return ResponseEntity.ok(
            gisMapDataService.filterPlots(
                    farmId,
                    user.getId(),
                    plotName,
                    plotStatus,
                    crop
            )
    );
}
}
