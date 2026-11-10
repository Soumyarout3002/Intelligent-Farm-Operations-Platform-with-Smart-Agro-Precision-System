package com.backend.service;

import com.backend.dto.equipment.EquipmentRequest;
import com.backend.dto.equipment.EquipmentResponse;
import com.backend.entity.Equipment;
import com.backend.entity.Farm;
import com.backend.repository.EquipmentRepository;
import com.backend.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.backend.repository.EquipmentBookingRepository;
import com.backend.repository.EquipmentUsageRepository;
import com.backend.repository.EquipmentBreakdownRepository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final FarmRepository farmRepository;
    private final EquipmentBookingRepository bookingRepository;
    private final EquipmentUsageRepository usageRepository;
    private final EquipmentBreakdownRepository breakdownRepository;

    public EquipmentResponse createEquipment(
            Long farmId,
            EquipmentRequest request,
            Long ownerId) {

        Farm farm = getOwnedFarm(farmId, ownerId);

        String equipmentName = request.getName().trim();

        if (equipmentRepository.existsByNameIgnoreCaseAndFarmId(
                equipmentName, farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Equipment with this name already exists in this farm"
            );
        }

        Equipment equipment = Equipment.builder()
                .farm(farm)
                .name(equipmentName)
                .type(request.getType().trim())
                .status("Available")
                .model(trimToNull(request.getModel()))
                .power(trimToNull(request.getPower()))
                .fuelType(trimToNull(request.getFuelType()))
                .manufacturingYear(request.getManufacturingYear())
                .condition(trimToNull(request.getCondition()))
                .nextMaintenanceDate(request.getNextMaintenanceDate())
                .description(trimToNull(request.getDescription()))
                .imageUrl(trimToNull(request.getImageUrl()))
                .build();

        return toResponse(equipmentRepository.save(equipment));
    }

    @Transactional(readOnly = true)
    public List<EquipmentResponse> getFarmEquipment(
            Long farmId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return equipmentRepository.findByFarmId(farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentById(
            Long farmId,
            Long equipmentId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return toResponse(getFarmEquipmentById(equipmentId, farmId));
    }

    public EquipmentResponse updateEquipment(
            Long farmId,
            Long equipmentId,
            EquipmentRequest request,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        Equipment equipment = getFarmEquipmentById(
                equipmentId, farmId);

        String equipmentName = request.getName().trim();

        boolean duplicateExists =
                equipmentRepository.existsByNameIgnoreCaseAndFarmId(
                        equipmentName, farmId);

        if (duplicateExists
                && !equipment.getName().equalsIgnoreCase(equipmentName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Equipment with this name already exists in this farm"
            );
        }

        equipment.setName(equipmentName);
        equipment.setType(request.getType().trim());
        equipment.setModel(trimToNull(request.getModel()));
        equipment.setPower(trimToNull(request.getPower()));
        equipment.setFuelType(trimToNull(request.getFuelType()));
        equipment.setManufacturingYear(request.getManufacturingYear());
        equipment.setCondition(trimToNull(request.getCondition()));
        equipment.setNextMaintenanceDate(
                request.getNextMaintenanceDate());
        equipment.setDescription(trimToNull(request.getDescription()));
        equipment.setImageUrl(trimToNull(request.getImageUrl()));

        // Status is deliberately not updated from the request.
        return toResponse(equipmentRepository.save(equipment));
    }

    public void deleteEquipment(
        Long farmId,
        Long equipmentId,
        Long ownerId) {

    getOwnedFarm(farmId, ownerId);

    Equipment equipment = getFarmEquipmentById(
            equipmentId, farmId);

    boolean hasBookings =
            bookingRepository.existsByEquipment_Id(equipmentId);

    boolean hasUsageRecords =
            usageRepository.existsByEquipment_Id(equipmentId);

    boolean hasBreakdownHistory =
            breakdownRepository.existsByEquipment_Id(equipmentId);

    if (hasBookings || hasUsageRecords || hasBreakdownHistory) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Equipment cannot be deleted because booking, usage, or breakdown history exists"
        );
    }

    equipmentRepository.delete(equipment);
}
    private Farm getOwnedFarm(Long farmId, Long ownerId) {
        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found"
                ));
    }

    private Equipment getFarmEquipmentById(
            Long equipmentId,
            Long farmId) {

        return equipmentRepository.findByIdAndFarmId(
                        equipmentId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Equipment not found"
                ));
    }

    private EquipmentResponse toResponse(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .farmId(equipment.getFarm().getId())
                .farm(equipment.getFarm().getName())
                .name(equipment.getName())
                .type(equipment.getType())
                .status(equipment.getStatus())
                .model(equipment.getModel())
                .power(equipment.getPower())
                .fuel(equipment.getFuelType())
                .year(equipment.getManufacturingYear())
                .condition(equipment.getCondition())
                .maintenance(equipment.getNextMaintenanceDate())
                .description(equipment.getDescription())
                .image(equipment.getImageUrl())
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}