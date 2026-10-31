package com.backend.controller;

import com.backend.dto.SoilRecordRequest;
import com.backend.dto.SoilRecordResponse;
import com.backend.entity.User;
import com.backend.service.SoilRecordService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/plots/{plotId}/soil-records")
@RequiredArgsConstructor
public class SoilRecordController {

    private final SoilRecordService soilRecordService;

    private Long getOwnerId(HttpServletRequest request) {
        Object authenticatedUser = request.getAttribute("authenticatedUser");

        if (!(authenticatedUser instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        return user.getId();
    }

    @PostMapping
    public ResponseEntity<SoilRecordResponse> createRecord(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @Valid @RequestBody SoilRecordRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(soilRecordService.createRecord(
                        farmId, plotId, getOwnerId(httpRequest), request));
    }

    @GetMapping
    public ResponseEntity<List<SoilRecordResponse>> getRecords(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(soilRecordService.getRecords(
                farmId, plotId, getOwnerId(httpRequest)));
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<SoilRecordResponse> getRecord(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long recordId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(soilRecordService.getRecord(
                farmId, plotId, recordId, getOwnerId(httpRequest)));
    }

    @PutMapping("/{recordId}")
    public ResponseEntity<SoilRecordResponse> updateRecord(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long recordId,
            @Valid @RequestBody SoilRecordRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(soilRecordService.updateRecord(
                farmId, plotId, recordId, getOwnerId(httpRequest), request));
    }

    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRecord(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long recordId,
            HttpServletRequest httpRequest) {

        soilRecordService.deleteRecord(
                farmId, plotId, recordId, getOwnerId(httpRequest));

        return ResponseEntity.noContent().build();
    }
}