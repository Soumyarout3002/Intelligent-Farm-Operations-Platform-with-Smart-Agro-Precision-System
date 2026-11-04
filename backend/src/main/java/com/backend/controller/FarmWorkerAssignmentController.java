package com.backend.controller;

import com.backend.dto.farm.WorkerAssignmentRequest;
import com.backend.dto.farm.WorkerAssignmentResponse;
import com.backend.entity.User;
import com.backend.service.FarmWorkerAssignmentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/workers")
public class FarmWorkerAssignmentController {

    private final FarmWorkerAssignmentService assignmentService;
    private final com.backend.repository.UserRepository userRepository;
    private final com.backend.service.FarmAccessService farmAccessService;

    public FarmWorkerAssignmentController(FarmWorkerAssignmentService assignmentService, com.backend.repository.UserRepository userRepository, com.backend.service.FarmAccessService farmAccessService) {
        this.assignmentService = assignmentService;
        this.userRepository = userRepository;
        this.farmAccessService = farmAccessService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<java.util.Map<String, Object>>> availableWorkers(@PathVariable Long farmId, HttpServletRequest request) {
        farmAccessService.requireManageable(farmId, getOwnerId(request));
        List<java.util.Map<String, Object>> workers = userRepository.findByRoleIgnoreCaseAndStatusIgnoreCase("FIELD_WORKER", "ACTIVE")
            .stream().map(worker -> java.util.Map.<String, Object>of("id", worker.getId(), "name", worker.getName(), "email", worker.getEmail(), "mobile", worker.getMobile() == null ? "" : worker.getMobile()))
            .toList();
        return ResponseEntity.ok(workers);
    }

    private Long getOwnerId(HttpServletRequest request) {

        Object authenticatedUser =
                request.getAttribute("authenticatedUser");

        if (!(authenticatedUser instanceof User user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        return user.getId();
    }

    @PostMapping
    public ResponseEntity<WorkerAssignmentResponse> assignWorker(
            @PathVariable Long farmId,
            @Valid @RequestBody WorkerAssignmentRequest request,
            HttpServletRequest httpRequest) {

        WorkerAssignmentResponse response =
                assignmentService.assignWorker(
                        farmId, getOwnerId(httpRequest), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkerAssignmentResponse>> getFarmWorkers(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                assignmentService.getFarmWorkers(
                        farmId, getOwnerId(httpRequest)));
    }

    @GetMapping("/{assignmentId}")
    public ResponseEntity<WorkerAssignmentResponse> getAssignment(
            @PathVariable Long farmId,
            @PathVariable Long assignmentId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                assignmentService.getAssignment(
                        farmId, assignmentId, getOwnerId(httpRequest)));
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<WorkerAssignmentResponse> updateAssignment(
            @PathVariable Long farmId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody WorkerAssignmentRequest request,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                assignmentService.updateAssignment(
                        farmId,
                        assignmentId,
                        getOwnerId(httpRequest),
                        request));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<Void> removeWorker(
            @PathVariable Long farmId,
            @PathVariable Long assignmentId,
            HttpServletRequest httpRequest) {

        assignmentService.removeWorker(
                farmId, assignmentId, getOwnerId(httpRequest));

        return ResponseEntity.noContent().build();
    }
}
