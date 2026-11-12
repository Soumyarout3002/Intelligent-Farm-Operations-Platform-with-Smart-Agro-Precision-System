
package com.backend.service;

import com.backend.dto.task.FarmTaskRequest;
import com.backend.dto.task.FarmTaskResponse;
import com.backend.entity.Farm;
import com.backend.entity.FarmTask;
import com.backend.entity.FarmWorkerAssignment;
import com.backend.entity.User;
import com.backend.repository.FarmRepository;
import com.backend.repository.FarmTaskRepository;
import com.backend.repository.FarmWorkerAssignmentRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmTaskService {

    private final FarmRepository farmRepository;
    private final FarmTaskRepository taskRepository;
    private final FarmWorkerAssignmentRepository assignmentRepository;

    public FarmTaskResponse createTask(
            Long farmId,
            Long ownerId,
            User createdBy,
            FarmTaskRequest request) {

        Farm farm = getOwnedFarm(farmId, ownerId);

        FarmWorkerAssignment assignment =
                assignmentRepository.findByIdAndFarmId(
                        request.getAssignmentId(), farmId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Worker assignment not found"));

        if (!"ACTIVE".equalsIgnoreCase(assignment.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign task to an inactive worker");
        }

        if (request.getDueDate() != null
                && request.getDueDate().isBefore(
                        java.time.LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Due date cannot be in the past");
        }

        FarmTask task = FarmTask.builder()
                .farm(farm)
                .assignedWorker(assignment)
                .createdBy(createdBy)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority().toUpperCase())
                .status("TODO")
                .build();

        return mapToResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<FarmTaskResponse> getFarmTasks(
            Long farmId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return taskRepository
                .findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public FarmTaskResponse updateTaskStatus(
            Long farmId,
            Long taskId,
            Long ownerId,
            String status) {

        getOwnedFarm(farmId, ownerId);

        FarmTask task = taskRepository.findByIdAndFarmId(
                        taskId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"));

        String normalizedStatus = status.trim().toUpperCase();

        if (!List.of("TODO", "IN_PROGRESS", "DONE")
                .contains(normalizedStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be TODO, IN_PROGRESS or DONE");
        }

        task.setStatus(normalizedStatus);

        return mapToResponse(taskRepository.save(task));
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {
        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found"));
    }

    private FarmTaskResponse mapToResponse(FarmTask task) {

        FarmWorkerAssignment assignment = task.getAssignedWorker();
        User worker = assignment.getWorker();

        return FarmTaskResponse.builder()
                .id(task.getId())
                .farmId(task.getFarm().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .assignmentId(assignment.getId())
                .workerId(worker.getId())
                .workerName(worker.getName())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .createdById(task.getCreatedBy().getId())
                .createdBy(task.getCreatedBy().getName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
