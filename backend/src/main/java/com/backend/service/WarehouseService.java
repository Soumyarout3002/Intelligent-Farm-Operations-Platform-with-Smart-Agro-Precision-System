
package com.backend.service;

import com.backend.dto.inventory.WarehouseRequest;
import com.backend.dto.inventory.WarehouseResponse;
import com.backend.entity.Farm;
import com.backend.entity.Warehouse;
import com.backend.repository.FarmRepository;
import com.backend.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final FarmRepository farmRepository;

    public WarehouseResponse createWarehouse(
            Long farmId,
            WarehouseRequest request,
            Long ownerId) {

        Farm farm = getOwnedFarm(farmId, ownerId);

        String warehouseName = request.getName().trim();

        if (warehouseRepository.existsByNameIgnoreCaseAndFarmId(
                warehouseName, farmId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A warehouse with this name already exists in this farm"
            );
        }

        Warehouse warehouse = Warehouse.builder()
                .farm(farm)
                .name(warehouseName)
                .location(request.getLocation().trim())
                .description(request.getDescription() == null
                        ? null
                        : request.getDescription().trim())
                .build();

        return toResponse(warehouseRepository.save(warehouse));
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> getFarmWarehouses(
            Long farmId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return warehouseRepository.findByFarmId(farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(
            Long farmId,
            Long warehouseId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return toResponse(getFarmWarehouse(warehouseId, farmId));
    }

    public WarehouseResponse updateWarehouse(
            Long farmId,
            Long warehouseId,
            WarehouseRequest request,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        Warehouse warehouse = getFarmWarehouse(warehouseId, farmId);

        String warehouseName = request.getName().trim();

        boolean duplicateExists =
                warehouseRepository.existsByNameIgnoreCaseAndFarmId(
                        warehouseName, farmId);

        if (duplicateExists
                && !warehouse.getName().equalsIgnoreCase(warehouseName)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A warehouse with this name already exists in this farm"
            );
        }

        warehouse.setName(warehouseName);
        warehouse.setLocation(request.getLocation().trim());
        warehouse.setDescription(request.getDescription() == null
                ? null
                : request.getDescription().trim());

        return toResponse(warehouseRepository.save(warehouse));
    }

    public void deleteWarehouse(
            Long farmId,
            Long warehouseId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        Warehouse warehouse = getFarmWarehouse(warehouseId, farmId);

        warehouseRepository.delete(warehouse);
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {
        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found"
                ));
    }

    private Warehouse getFarmWarehouse(
            Long warehouseId,
            Long farmId) {

        return warehouseRepository.findByIdAndFarmId(
                        warehouseId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Warehouse not found"
                ));
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .farmId(warehouse.getFarm().getId())
                .name(warehouse.getName())
                .location(warehouse.getLocation())
                .description(warehouse.getDescription())
                .status(warehouse.getStatus())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }
}
