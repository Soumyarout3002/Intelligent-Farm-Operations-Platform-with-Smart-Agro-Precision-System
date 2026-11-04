
package com.backend.service;

import com.backend.entity.Farm;
import com.backend.entity.User;
import com.backend.repository.FarmRepository;
import com.backend.repository.FarmWorkerAssignmentRepository;
import com.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class FarmAccessService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final FarmWorkerAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public Farm requireReadable(Long farmId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        String role = user.getRole();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return findFarm(farmId);
        }

        if ("FARMER".equalsIgnoreCase(role)) {
            return farmRepository.findByIdAndOwnerId(farmId, userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
        }

        if ("FIELD_WORKER".equalsIgnoreCase(role)
                && isAssignedWorker(farmId, userId)) {
            return findFarm(farmId);
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Farm not found");
    }

    // General farm management: Admin and farm owner only.
    @Transactional(readOnly = true)
    public Farm requireManageable(Long farmId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        String role = user.getRole();

        if ("FIELD_WORKER".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Field workers cannot manage general farm details.");
        }

        if ("ADMIN".equalsIgnoreCase(role)) {
            return findFarm(farmId);
        }

        if ("FARMER".equalsIgnoreCase(role)) {
            return farmRepository.findByIdAndOwnerId(farmId, userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "You do not have permission.");
    }

    // Soil records: Admin, farm owner and assigned Field Worker.
    @Transactional(readOnly = true)
    public Farm requireSoilRecordWritable(Long farmId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        String role = user.getRole();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return findFarm(farmId);
        }

        if ("FARMER".equalsIgnoreCase(role)) {
            return farmRepository.findByIdAndOwnerId(farmId, userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
        }

        if ("FIELD_WORKER".equalsIgnoreCase(role)
                && isAssignedWorker(farmId, userId)) {
            return findFarm(farmId);
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorized to add or edit soil records for this farm.");
    }

    private boolean isAssignedWorker(Long farmId, Long userId) {

        return assignmentRepository
                .findByWorkerIdAndStatusIgnoreCase(userId, "ACTIVE")
                .stream()
                .anyMatch(assignment ->
                        assignment.getFarm() != null
                                && farmId.equals(assignment.getFarm().getId()));
    }

    private Farm findFarm(Long farmId) {

        return farmRepository.findById(farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));
    }
}
