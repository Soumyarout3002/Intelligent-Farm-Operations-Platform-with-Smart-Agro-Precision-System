
package com.backend.controller;

import com.backend.dto.task.FarmTaskRequest;
import com.backend.dto.task.FarmTaskResponse;
import com.backend.entity.User;
import com.backend.service.FarmTaskService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/tasks")
@RequiredArgsConstructor
public class FarmTaskController {

    private final FarmTaskService taskService;

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

    // Create a new task
    @PostMapping
    public ResponseEntity<FarmTaskResponse> createTask(
            @PathVariable Long farmId,
            @Valid @RequestBody FarmTaskRequest request,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        FarmTaskResponse response = taskService.createTask(
                farmId,
                user.getId(),
                user,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all tasks of a farm
    @GetMapping
    public ResponseEntity<List<FarmTaskResponse>> getFarmTasks(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                taskService.getFarmTasks(farmId, user.getId())
        );
    }

    // Update task status
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<FarmTaskResponse> updateTaskStatus(
            @PathVariable Long farmId,
            @PathVariable Long taskId,
            @RequestParam String status,
            HttpServletRequest httpRequest) {

        User user = getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                taskService.updateTaskStatus(
                        farmId,
                        taskId,
                        user.getId(),
                        status
                )
        );
    }
}
