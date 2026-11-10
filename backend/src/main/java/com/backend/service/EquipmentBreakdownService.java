
package com.backend.service;

import com.backend.dto.equipment.EquipmentBreakdownRequest;
import com.backend.dto.equipment.EquipmentBreakdownResponse;
import com.backend.entity.Equipment;
import com.backend.entity.EquipmentBreakdown;
import com.backend.entity.User;
import com.backend.repository.EquipmentBreakdownRepository;
import com.backend.repository.EquipmentRepository;
import com.backend.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentBreakdownService {

    private final EquipmentBreakdownRepository breakdownRepository;
    private final EquipmentRepository equipmentRepository;
    private final FarmRepository farmRepository;

    public EquipmentBreakdownResponse reportBreakdown(
            Long farmId,
            EquipmentBreakdownRequest request,
            User user) {

        farmRepository.findByIdAndOwnerId(farmId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found or access denied"));

        Equipment equipment = equipmentRepository
                .findByIdAndFarmId(request.getEquipmentId(), farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Equipment not found in this farm"));

        if (hasActiveBreakdown(equipment.getId(), farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An active breakdown already exists for this equipment");
        }

        EquipmentBreakdown breakdown = EquipmentBreakdown.builder()
                .equipment(equipment)
                .reportedBy(user)
                .reason(request.getReason().trim())
                .description(request.getDescription().trim())
                .status("REPORTED")
                .build();

        equipment.setStatus("Maintenance");

        breakdownRepository.save(breakdown);

        return toResponse(breakdown);
    }

    @Transactional(readOnly = true)
    public List<EquipmentBreakdownResponse> getBreakdowns(
            Long farmId, User user) {

        verifyFarmAccess(farmId, user);

        return breakdownRepository
                .findBreakdownsByFarmId(farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EquipmentBreakdownResponse updateBreakdownStatus(
            Long farmId,
            Long breakdownId,
            String newStatus,
            User admin) {

        verifyAdmin(admin);

        EquipmentBreakdown breakdown = breakdownRepository
                .findBreakdownByIdAndFarmId(breakdownId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Breakdown report not found"));

        String status = newStatus.trim().toUpperCase(Locale.ROOT);

        if (!List.of("UNDER_REPAIR", "RESOLVED").contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be UNDER_REPAIR or RESOLVED");
        }

        if ("RESOLVED".equalsIgnoreCase(breakdown.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Resolved breakdown cannot be updated");
        }

        if ("RESOLVED".equals(status)) {
            breakdown.setStatus("RESOLVED");
            breakdown.setResolvedBy(admin);
            breakdown.setResolvedAt(LocalDateTime.now());

            Equipment equipment = breakdown.getEquipment();

            if (!hasOtherActiveBreakdown(
                    equipment.getId(), farmId, breakdown.getId())) {
                equipment.setStatus("Available");
            }
        } else {
            breakdown.setStatus("UNDER_REPAIR");
            breakdown.setEquipment(
                    breakdown.getEquipment());
            breakdown.getEquipment().setStatus("Maintenance");
        }

        return toResponse(breakdownRepository.save(breakdown));
    }

    private void verifyFarmAccess(Long farmId, User user) {
        if (isAdmin(user)) {
            farmRepository.findById(farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Farm not found"));
            return;
        }

        farmRepository.findByIdAndOwnerId(farmId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found or access denied"));
    }

    private void verifyAdmin(User user) {
        if (!isAdmin(user)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only administrators can update breakdown status");
        }
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null
                && "ADMIN".equalsIgnoreCase(user.getRole().trim());
    }

    private boolean hasActiveBreakdown(Long equipmentId, Long farmId) {
        return breakdownRepository.existsActiveBreakdown(equipmentId);
    }

    private boolean hasOtherActiveBreakdown(
        Long equipmentId, Long farmId, Long currentBreakdownId) {

        return breakdownRepository.existsOtherActiveBreakdown(
                equipmentId, currentBreakdownId);
    }

    private EquipmentBreakdownResponse toResponse(
            EquipmentBreakdown breakdown) {

        Equipment equipment = breakdown.getEquipment();
        User reporter = breakdown.getReportedBy();
        User resolver = breakdown.getResolvedBy();

        return EquipmentBreakdownResponse.builder()
                .id(breakdown.getId())
                .equipmentId(equipment.getId())
                .equipment(equipment.getName())
                .reportedById(reporter.getId())
                .reportedBy(reporter.getName())
                .reason(breakdown.getReason())
                .description(breakdown.getDescription())
                .status(breakdown.getStatus())
                .resolvedById(resolver != null ? resolver.getId() : null)
                .resolvedBy(resolver != null ? resolver.getName() : null)
                .reportedAt(breakdown.getReportedAt())
                .resolvedAt(breakdown.getResolvedAt())
                .updatedAt(breakdown.getUpdatedAt())
                .build();
    }
}
