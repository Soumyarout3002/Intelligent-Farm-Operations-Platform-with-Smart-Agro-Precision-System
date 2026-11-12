
package com.backend.controller;

import com.backend.dto.labour.LabourAttendanceRequest;
import com.backend.dto.labour.LabourAttendanceResponse;
import com.backend.entity.User;
import com.backend.service.LabourAttendanceService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/attendance")
@RequiredArgsConstructor
public class LabourAttendanceController {

    private final LabourAttendanceService attendanceService;

    private User getAuthenticatedUser(HttpServletRequest request) {

        Object authenticatedUser =
                request.getAttribute("authenticatedUser");

        if (!(authenticatedUser instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return user;
    }

    @PostMapping
    public ResponseEntity<LabourAttendanceResponse> markAttendance(
            @PathVariable Long farmId,
            @Valid @RequestBody LabourAttendanceRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        LabourAttendanceResponse response =
                attendanceService.markAttendance(
                        farmId,
                        user.getId(),
                        user,
                        request
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<LabourAttendanceResponse>> getAttendance(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                attendanceService.getFarmAttendance(
                        farmId,
                        user.getId()
                )
        );
    }


    @PatchMapping("/{attendanceId}/crop-cycle/{cropCycleId}")
    public ResponseEntity<LabourAttendanceResponse> assignCropCycle(
            @PathVariable Long farmId,
            @PathVariable Long attendanceId,
            @PathVariable Long cropCycleId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                attendanceService.assignCropCycle(
                        farmId,
                        attendanceId,
                        cropCycleId,
                        user.getId()
                )
        );
    }

}
