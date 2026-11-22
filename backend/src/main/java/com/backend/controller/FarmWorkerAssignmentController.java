
package com.backend.controller;

import com.backend.dto.farm.WorkerAssignmentRequest;
import com.backend.dto.farm.WorkerAssignmentResponse;
import com.backend.entity.User;
import com.backend.service.FarmWorkerAssignmentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/workers")
@RequiredArgsConstructor
public class FarmWorkerAssignmentController {

    private final FarmWorkerAssignmentService assignmentService;

    private User getAuthenticatedUser(HttpServletRequest request) {
        Object authenticatedUser = request.getAttribute("authenticatedUser");

        if (!(authenticatedUser instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return user;
    }

    @PostMapping
    public ResponseEntity<WorkerAssignmentResponse> assignWorker(
            @PathVariable Long farmId,
            @Valid @RequestBody WorkerAssignmentRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        WorkerAssignmentResponse response = assignmentService.assignWorker(
                farmId, user.getId(), user.getRole(), request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkerAssignmentResponse>> getFarmWorkers(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                assignmentService.getFarmWorkers(
                        farmId, user.getId(), user.getRole()
                )
        );
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<WorkerAssignmentResponse> getAssignment(
            @PathVariable Long farmId,
            @PathVariable Long assignmentId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                assignmentService.getAssignment(
                        farmId, assignmentId, user.getId(), user.getRole()
                )
        );
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<WorkerAssignmentResponse> updateAssignment(
            @PathVariable Long farmId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody WorkerAssignmentRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                assignmentService.updateAssignment(
                        farmId, assignmentId, user.getId(),
                        user.getRole(), request
                )
        );
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> removeWorker(
            @PathVariable Long farmId,
            @PathVariable Long assignmentId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        assignmentService.removeWorker(
                farmId, assignmentId, user.getId(), user.getRole()
        );

        return ResponseEntity.noContent().build();
    }
}
