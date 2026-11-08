package com.backend.controller;

import com.backend.dto.irrigation.*;
import com.backend.entity.User;
import com.backend.service.IrrigationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/irrigation")
@RequiredArgsConstructor
public class IrrigationController {

    private final IrrigationService irrigationService;

    // Create irrigation schedule

    @PostMapping("/schedules")
    public ResponseEntity<IrrigationScheduleResponse> createSchedule(
            @PathVariable Long farmId,
            @Valid @RequestBody IrrigationScheduleRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(irrigationService.createSchedule(
                        farmId,
                        authenticatedUser(httpRequest).getId(),
                        request
                ));
    }

    // Get all schedules

    @GetMapping("/schedules")
    public List<IrrigationScheduleResponse> getSchedules(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        return irrigationService.getSchedules(
                farmId,
                authenticatedUser(httpRequest).getId()
        );
    }

    // Get one schedule

    @GetMapping("/schedules/{scheduleId}")
    public IrrigationScheduleResponse getSchedule(
            @PathVariable Long farmId,
            @PathVariable Long scheduleId,
            HttpServletRequest httpRequest) {

        return irrigationService.getSchedule(
                farmId,
                scheduleId,
                authenticatedUser(httpRequest).getId()
        );
    }

    // Update schedule

    @PutMapping("/schedules/{scheduleId}")
    public IrrigationScheduleResponse updateSchedule(
            @PathVariable Long farmId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody IrrigationScheduleRequest request,
            HttpServletRequest httpRequest) {

        return irrigationService.updateSchedule(
                farmId,
                scheduleId,
                authenticatedUser(httpRequest).getId(),
                request
        );
    }

    // Delete schedule

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long farmId,
            @PathVariable Long scheduleId,
            HttpServletRequest httpRequest) {

        irrigationService.deleteSchedule(
                farmId,
                scheduleId,
                authenticatedUser(httpRequest).getId()
        );

        return ResponseEntity.noContent().build();
    }

    // Record completed or skipped irrigation

    @PostMapping("/schedules/{scheduleId}/record")
    public ResponseEntity<IrrigationRecordResponse> recordIrrigation(
            @PathVariable Long farmId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody IrrigationRecordRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(irrigationService.recordIrrigation(
                        farmId,
                        scheduleId,
                        authenticatedUser(httpRequest).getId(),
                        request
                ));
    }

    // Get irrigation history

    @GetMapping("/schedules/{scheduleId}/records")
    public List<IrrigationRecordResponse> getRecords(
            @PathVariable Long farmId,
            @PathVariable Long scheduleId,
            HttpServletRequest httpRequest) {

        return irrigationService.getRecords(
                farmId,
                scheduleId,
                authenticatedUser(httpRequest).getId()
        );
    }

    // Weather-based suggestions

    @GetMapping("/suggestions")
    public List<IrrigationSuggestionResponse> getSuggestions(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        return irrigationService.getSuggestions(
                farmId,
                authenticatedUser(httpRequest).getId()
        );
    }

    // Existing authentication integration

    private User authenticatedUser(HttpServletRequest request) {

        Object value = request.getAttribute("authenticatedUser");

        if (value instanceof User user) {
            return user;
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required"
        );
    }
}