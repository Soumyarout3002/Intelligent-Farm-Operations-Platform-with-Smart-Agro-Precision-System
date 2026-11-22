
package com.backend.service;

import com.backend.dto.farm.WorkerAssignmentRequest;
import com.backend.dto.farm.WorkerAssignmentResponse;
import com.backend.entity.Farm;
import com.backend.entity.FarmWorkerAssignment;
import com.backend.entity.User;
import com.backend.repository.FarmRepository;
import com.backend.repository.FarmWorkerAssignmentRepository;
import com.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmWorkerAssignmentService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final FarmWorkerAssignmentRepository assignmentRepository;

    public WorkerAssignmentResponse assignWorker(
            Long farmId,
            Long userId,
            String role,
            WorkerAssignmentRequest request) {

        Farm farm = getManageableFarm(farmId, userId, role);

        User worker = userRepository.findById(request.getWorkerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Worker not found"));

        if (!"FIELD_WORKER".equalsIgnoreCase(worker.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected user is not a field worker");
        }

        if (!"ACTIVE".equalsIgnoreCase(worker.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot assign an inactive worker");
        }

        if (assignmentRepository.existsByFarmIdAndWorkerId(
                farmId, worker.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Worker is already assigned to this farm");
        }

        FarmWorkerAssignment assignment =
                FarmWorkerAssignment.builder()
                        .farm(farm)
                        .worker(worker)
                        .assignmentRole(request.getAssignmentRole())
                        .status(request.getStatus() == null
                                ? "ACTIVE" : request.getStatus())
                        .notes(request.getNotes())
                        .build();

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<WorkerAssignmentResponse> getFarmWorkers(
            Long farmId,
            Long userId,
            String role) {

        getReadableFarm(farmId, userId, role);

        return assignmentRepository.findByFarmId(farmId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkerAssignmentResponse getAssignment(
            Long farmId,
            Long assignmentId,
            Long userId,
            String role) {

        getReadableFarm(farmId, userId, role);

        return mapToResponse(findAssignment(farmId, assignmentId));
    }

    public WorkerAssignmentResponse updateAssignment(
            Long farmId,
            Long assignmentId,
            Long userId,
            String role,
            WorkerAssignmentRequest request) {

        getManageableFarm(farmId, userId, role);

        FarmWorkerAssignment assignment =
                findAssignment(farmId, assignmentId);

        assignment.setAssignmentRole(request.getAssignmentRole());
        assignment.setNotes(request.getNotes());

        if (request.getStatus() != null) {
            assignment.setStatus(request.getStatus());
        }

        return mapToResponse(assignmentRepository.save(assignment));
    }

    public void removeWorker(
            Long farmId,
            Long assignmentId,
            Long userId,
            String role) {

        getManageableFarm(farmId, userId, role);

        FarmWorkerAssignment assignment =
                findAssignment(farmId, assignmentId);

        assignmentRepository.delete(assignment);
    }

    private Farm getReadableFarm(
            Long farmId, Long userId, String role) {

        if ("ADMIN".equalsIgnoreCase(role)) {
            return farmRepository.findById(farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
        }

        if ("FARMER".equalsIgnoreCase(role)) {
            return farmRepository.findByIdAndOwnerId(farmId, userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to view farm workers");
    }

    private Farm getManageableFarm(
            Long farmId, Long userId, String role) {

        if ("ADMIN".equalsIgnoreCase(role)) {
            return farmRepository.findById(farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
        }

        if ("FARMER".equalsIgnoreCase(role)) {
            return farmRepository.findByIdAndOwnerId(farmId, userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not allowed to manage farm workers");
    }

    private FarmWorkerAssignment findAssignment(
            Long farmId, Long assignmentId) {

        return assignmentRepository
                .findByIdAndFarmId(assignmentId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Worker assignment not found"));
    }

    private WorkerAssignmentResponse mapToResponse(
            FarmWorkerAssignment assignment) {

        User worker = assignment.getWorker();

        return WorkerAssignmentResponse.builder()
                .id(assignment.getId())
                .farmId(assignment.getFarm().getId())
                .farmName(assignment.getFarm().getName())
                .workerId(worker.getId())
                .workerName(worker.getName())
                .workerEmail(worker.getEmail())
                .workerMobile(worker.getMobile())
                .assignmentRole(assignment.getAssignmentRole())
                .status(assignment.getStatus())
                .notes(assignment.getNotes())
                .assignedAt(assignment.getAssignedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }
}
