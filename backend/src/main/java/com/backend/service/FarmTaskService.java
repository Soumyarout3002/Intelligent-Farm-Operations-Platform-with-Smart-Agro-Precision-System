
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

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

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

        Farm farm = getFarmForTaskCreation(farmId, ownerId, createdBy);

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
                && request.getDueDate().isBefore(LocalDate.now())) {
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
                .priority(request.getPriority().toUpperCase(Locale.ROOT))
                .status("TODO")
                .build();

        return mapToResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<FarmTaskResponse> getFarmTasks(
            Long farmId,
            User user) {

        verifyFarmReadAccess(farmId, user);

        return taskRepository
                .findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .filter(task -> !isFieldWorker(user)
                        || task.getAssignedWorker()
                        .getWorker()
                        .getId()
                        .equals(user.getId()))
                .map(this::mapToResponse)
                .toList();
    }

    public FarmTaskResponse updateTaskStatus(
            Long farmId,
            Long taskId,
            User user,
            String status) {

        verifyFarmReadAccess(farmId, user);

        FarmTask task = taskRepository.findByIdAndFarmId(
                        taskId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"));

        if (isFieldWorker(user)
                && !task.getAssignedWorker()
                .getWorker()
                .getId()
                .equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can update only tasks assigned to you");
        }

        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);

        if (!List.of("TODO", "IN_PROGRESS", "DONE")
                .contains(normalizedStatus)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be TODO, IN_PROGRESS or DONE");
        }

        task.setStatus(normalizedStatus);

        return mapToResponse(taskRepository.save(task));
    }

    private Farm getFarmForTaskCreation(
            Long farmId,
            Long ownerId,
            User createdBy) {

        String role = createdBy.getRole();

        if (role != null && "ADMIN".equalsIgnoreCase(role)) {
            return farmRepository.findById(farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Farm not found"));
        }

        if (isFieldWorker(createdBy)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Field workers cannot assign tasks");
        }

        if (role != null && "FARMER".equalsIgnoreCase(role)) {
            return getOwnedFarm(farmId, ownerId);
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to assign farm tasks");
    }

    private void verifyFarmReadAccess(Long farmId, User user) {

        if (user.getRole() != null
                && "ADMIN".equalsIgnoreCase(user.getRole())) {

            if (!farmRepository.existsById(farmId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found");
            }
            return;
        }

        if (isFieldWorker(user)) {

            boolean assigned = assignmentRepository
                    .findByWorker_IdAndStatusIgnoreCase(
                            user.getId(), "ACTIVE")
                    .stream()
                    .anyMatch(a -> a.getFarm() != null
                            && a.getFarm().getId().equals(farmId));

            if (!assigned) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found or not assigned to this worker");
            }
            return;
        }

        getOwnedFarm(farmId, user.getId());
    }

    private boolean isFieldWorker(User user) {
        return user.getRole() != null
                && "FIELD_WORKER".equalsIgnoreCase(user.getRole());
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
