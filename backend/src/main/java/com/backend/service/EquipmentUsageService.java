
package com.backend.service;

import com.backend.dto.equipment.EquipmentUsageRequest;
import com.backend.dto.equipment.EquipmentUsageResponse;
import com.backend.entity.Equipment;
import com.backend.entity.EquipmentUsage;
import com.backend.entity.Farm;
import com.backend.entity.User;
import com.backend.repository.EquipmentRepository;
import com.backend.repository.EquipmentUsageRepository;
import com.backend.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.backend.entity.CropCycle;
import com.backend.repository.CropCycleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentUsageService {

    private final EquipmentUsageRepository usageRepository;
    private final EquipmentRepository equipmentRepository;
    private final FarmRepository farmRepository;
    private final CropCycleRepository cropCycleRepository;

    public EquipmentUsageResponse recordUsage(
            Long farmId,
            EquipmentUsageRequest request,
            User user) {

        Farm farm = farmRepository.findByIdAndOwnerId(farmId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found or access denied"));

        if (request.getUsageDate() == null
                || request.getUsageDate().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usage date cannot be in the future");
        }

        Equipment equipment = equipmentRepository
                .findByIdAndFarmId(request.getEquipmentId(), farm.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Equipment not found in this farm"));

        if (request.getOperatingHours() == null
                || request.getOperatingHours().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Operating hours must be greater than zero");
        }

        // Validate optional crop cycle
        CropCycle cropCycle = null;

        if (request.getCropCycleId() != null) {

            cropCycle = cropCycleRepository.findById(request.getCropCycleId())
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

        BigDecimal fuelCost = zeroIfNull(request.getFuelCost());
        BigDecimal usageCost = zeroIfNull(request.getUsageCost());

        EquipmentUsage usage = EquipmentUsage.builder()
                .equipment(equipment)
                .cropCycle(cropCycle)
                .recordedBy(user)
                .usageDate(request.getUsageDate())
                .operatingHours(request.getOperatingHours())
                .fuelConsumed(request.getFuelConsumed())
                .fuelCost(fuelCost)
                .usageCost(usageCost)
                .notes(trimToNull(request.getNotes()))
                .build();

        return toResponse(usageRepository.save(usage));
    }


    public EquipmentUsageResponse assignCropCycle(
            Long farmId,
            Long usageId,
            Long cropCycleId,
            Long ownerId) {

        Farm farm = farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found or access denied"));

        EquipmentUsage usage = usageRepository
        .findByEquipmentFarmIdOrderByUsageDateDesc(farm.getId())
        .stream()
        .filter(item -> item.getId().equals(usageId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Equipment usage record not found"));

        CropCycle cropCycle = cropCycleRepository.findById(cropCycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Crop cycle not found"));

        if (!cropCycle.getPlot().getFarm().getId().equals(farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Crop cycle not found in this farm");
        }

        usage.setCropCycle(cropCycle);

        return toResponse(usageRepository.save(usage));
    }

    @Transactional(readOnly = true)
    public List<EquipmentUsageResponse> getFarmUsage(
            Long farmId, Long ownerId) {

        verifyFarmOwnership(farmId, ownerId);

        return usageRepository
                .findByEquipmentFarmIdOrderByUsageDateDesc(farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EquipmentUsageResponse> getEquipmentUsage(
            Long farmId, Long equipmentId, Long ownerId) {

        verifyFarmOwnership(farmId, ownerId);

        if (!equipmentRepository.existsByIdAndFarmId(equipmentId, farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Equipment not found in this farm");
        }

        return usageRepository
                .findByEquipmentIdAndEquipmentFarmIdOrderByUsageDateDesc(
                        equipmentId, farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void verifyFarmOwnership(Long farmId, Long ownerId) {
        if (!farmRepository.findByIdAndOwnerId(farmId, ownerId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Farm not found or access denied");
        }
    }


    private EquipmentUsageResponse toResponse(EquipmentUsage usage) {
        BigDecimal fuelCost = zeroIfNull(usage.getFuelCost());
        BigDecimal usageCost = zeroIfNull(usage.getUsageCost());

        return EquipmentUsageResponse.builder()
                .id(usage.getId())
                .equipmentId(usage.getEquipment().getId())
                .cropCycleId(
                        usage.getCropCycle() != null
                                ? usage.getCropCycle().getId()
                                : null
                )
                .equipment(usage.getEquipment().getName())
                .recordedById(usage.getRecordedBy().getId())
                .recordedBy(usage.getRecordedBy().getName())
                .usageDate(usage.getUsageDate())
                .operatingHours(usage.getOperatingHours())
                .fuelConsumed(usage.getFuelConsumed())
                .fuelCost(fuelCost)
                .usageCost(usageCost)
                .totalCost(fuelCost.add(usageCost))
                .notes(usage.getNotes())
                .createdAt(usage.getCreatedAt())
                .build();
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
