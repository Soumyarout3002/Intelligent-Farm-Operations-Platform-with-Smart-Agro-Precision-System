package com.backend.service;

import com.backend.dto.inventory.StockMovementRequest;
import com.backend.dto.inventory.StockMovementResponse;
import com.backend.entity.*;
import com.backend.entity.StockMovement.MovementType;
import com.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final FarmRepository farmRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;

    public StockMovementResponse stockIn(
            Long farmId,
            Long itemId,
            Long userId,
            StockMovementRequest request) {

        return processMovement(
                farmId, itemId, userId, request, MovementType.IN);
    }

    public StockMovementResponse stockOut(
            Long farmId,
            Long itemId,
            Long userId,
            StockMovementRequest request) {

        return processMovement(
                farmId, itemId, userId, request, MovementType.OUT);
    }

    private StockMovementResponse processMovement(
            Long farmId,
            Long itemId,
            Long userId,
            StockMovementRequest request,
            MovementType type) {

        Farm farm = farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

       InventoryItem item = inventoryItemRepository
        .findByIdAndFarmIdForUpdate(itemId, farmId)
        .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Inventory item not found"));

        BigDecimal movementQuantity = request.getQuantity();

        // Validate optional supplier
        Supplier supplier = null;

        if (request.getSupplierId() != null) {
            supplier = supplierRepository
                    .findByIdAndFarmId(request.getSupplierId(), farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Supplier not found"));
        }

        // Validate optional warehouse
        Warehouse warehouse = null;

        if (request.getWarehouseId() != null) {
            warehouse = warehouseRepository
                    .findByIdAndFarmId(request.getWarehouseId(), farmId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Warehouse not found"));
        }

        BigDecimal currentQuantity = item.getQuantity();
        BigDecimal updatedQuantity;

        if (type == MovementType.IN) {
            updatedQuantity = currentQuantity.add(movementQuantity);
        } else {
            if (currentQuantity.compareTo(movementQuantity) < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock. Available quantity: "
                                + currentQuantity);
            }

            updatedQuantity = currentQuantity.subtract(movementQuantity);
        }

        // Update inventory quantity
        item.setQuantity(updatedQuantity);
        inventoryItemRepository.save(item);

        // Save stock movement history
        StockMovement movement = StockMovement.builder()
                .farm(farm)
                .inventoryItem(item)
                .type(type)
                .quantity(movementQuantity)
                .supplier(supplier)
                .warehouse(warehouse)
                .remarks(request.getRemarks())
                .build();

        StockMovement savedMovement =
                stockMovementRepository.save(movement);

        return toResponse(savedMovement, updatedQuantity);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getItemHistory(
            Long farmId,
            Long itemId,
            Long userId) {

        verifyFarmOwnership(farmId, userId);

        if (!inventoryItemRepository.findByIdAndFarmId(itemId, farmId)
                .isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Inventory item not found");
        }

        return stockMovementRepository
                .findByFarmIdAndInventoryItemIdOrderByCreatedAtDesc(
                        farmId, itemId)
                .stream()
                .map(movement -> toResponse(
                        movement, movement.getInventoryItem().getQuantity()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> getFarmHistory(
            Long farmId,
            Long userId) {

        verifyFarmOwnership(farmId, userId);

        return stockMovementRepository
                .findByFarmIdOrderByCreatedAtDesc(farmId)
                .stream()
                .map(movement -> toResponse(
                        movement, movement.getInventoryItem().getQuantity()))
                .toList();
    }

    private void verifyFarmOwnership(Long farmId, Long userId) {
        farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));
    }

    private StockMovementResponse toResponse(
            StockMovement movement,
            BigDecimal availableQuantity) {

        return StockMovementResponse.builder()
                .id(movement.getId())
                .farmId(movement.getFarm().getId())
                .inventoryItemId(movement.getInventoryItem().getId())
                .itemName(movement.getInventoryItem().getName())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .availableQuantity(availableQuantity)
                .supplierId(movement.getSupplier() != null
                        ? movement.getSupplier().getId() : null)
                .supplierName(movement.getSupplier() != null
                        ? movement.getSupplier().getName() : null)
                .warehouseId(movement.getWarehouse() != null
                        ? movement.getWarehouse().getId() : null)
                .warehouseName(movement.getWarehouse() != null
                        ? movement.getWarehouse().getName() : null)
                .remarks(movement.getRemarks())
                .createdAt(movement.getCreatedAt())
                .build();
    }
}