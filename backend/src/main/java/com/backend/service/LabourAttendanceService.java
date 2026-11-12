
package com.backend.service;

import com.backend.dto.labour.LabourAttendanceRequest;
import com.backend.dto.labour.LabourAttendanceResponse;
import com.backend.entity.CropCycle;
import com.backend.entity.FarmWorkerAssignment;
import com.backend.entity.LabourAttendance;
import com.backend.entity.User;
import com.backend.repository.CropCycleRepository;
import com.backend.repository.FarmRepository;
import com.backend.repository.FarmWorkerAssignmentRepository;
import com.backend.repository.LabourAttendanceRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LabourAttendanceService {

    private final FarmRepository farmRepository;
    private final FarmWorkerAssignmentRepository assignmentRepository;
    private final LabourAttendanceRepository attendanceRepository;
    private final CropCycleRepository cropCycleRepository;

    public LabourAttendanceResponse markAttendance(
            Long farmId,
            Long ownerId,
            User recordedBy,
            LabourAttendanceRequest request) {

        // Verify farm ownership
        farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        // Verify worker assignment belongs to this farm
        FarmWorkerAssignment assignment =
                assignmentRepository.findByIdAndFarmId(
                        request.getAssignmentId(), farmId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Worker assignment not found"));

        // Only active assignments can receive attendance
        if (!"ACTIVE".equalsIgnoreCase(assignment.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot mark attendance for an inactive worker");
        }

        // Validate optional crop cycle
        CropCycle cropCycle = null;

        if (request.getCropCycleId() != null) {

            cropCycle = cropCycleRepository.findById(
                    request.getCropCycleId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Crop cycle not found"));

            // Ensure crop cycle belongs to the same farm
            if (!cropCycle.getPlot().getFarm().getId().equals(farmId)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Crop cycle not found in this farm");
            }
        }

        LocalDate attendanceDate = request.getAttendanceDate();

        // Future attendance is not allowed
        if (attendanceDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Future attendance cannot be marked");
        }

        // Prevent duplicate attendance
        if (attendanceRepository.existsByAssignmentIdAndAttendanceDate(
                assignment.getId(), attendanceDate)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance already exists for this worker on this date");
        }

        String status = request.getStatus().trim().toUpperCase();

        BigDecimal dailyWage;

        if ("PRESENT".equals(status)) {

            if (request.getDailyWage() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Daily wage is required for present workers");
            }

            dailyWage = request.getDailyWage();

        } else if ("ABSENT".equals(status)) {

            dailyWage = BigDecimal.ZERO;

        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be PRESENT or ABSENT");
        }

        LabourAttendance attendance = LabourAttendance.builder()
                .assignment(assignment)
                .cropCycle(cropCycle)
                .recordedBy(recordedBy)
                .attendanceDate(attendanceDate)
                .status(status)
                .dailyWage(dailyWage)
                .notes(request.getNotes())
                .build();

        return mapToResponse(
                attendanceRepository.save(attendance));
    }

    @Transactional(readOnly = true)
    public List<LabourAttendanceResponse> getFarmAttendance(
            Long farmId,
            Long ownerId) {

        farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        return attendanceRepository
                .findByAssignmentFarmIdOrderByAttendanceDateDesc(farmId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    public LabourAttendanceResponse assignCropCycle(
            Long farmId,
            Long attendanceId,
            Long cropCycleId,
            Long ownerId) {

        // Verify farm ownership
        farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        // Find attendance belonging to this farm
        LabourAttendance attendance = attendanceRepository
                .findByIdAndAssignmentFarmId(attendanceId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Attendance not found"));

        // Verify crop cycle exists
        CropCycle cropCycle = cropCycleRepository.findById(cropCycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crop cycle not found"));

        // Ensure crop cycle belongs to this farm
        if (!cropCycle.getPlot().getFarm().getId().equals(farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Crop cycle not found in this farm");
        }

        attendance.setCropCycle(cropCycle);

        return mapToResponse(attendanceRepository.save(attendance));
    }

    private LabourAttendanceResponse mapToResponse(
            LabourAttendance attendance) {

        FarmWorkerAssignment assignment = attendance.getAssignment();
        User worker = assignment.getWorker();

        return LabourAttendanceResponse.builder()
                .id(attendance.getId())
                .assignmentId(assignment.getId())
                .cropCycleId(
                        attendance.getCropCycle() != null
                            ? attendance.getCropCycle().getId()
                            : null
                    )
                .workerId(worker.getId())
                .workerName(worker.getName())
                .attendanceDate(attendance.getAttendanceDate().toString())
                .status(attendance.getStatus())
                .dailyWage(attendance.getDailyWage())
                .notes(attendance.getNotes())
                .createdAt(attendance.getCreatedAt())
                .build();
    }
}
