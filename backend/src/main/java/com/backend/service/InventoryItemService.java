
package com.backend.service;

import com.backend.dto.inventory.InventoryItemRequest;
import com.backend.dto.inventory.InventoryItemResponse;
import com.backend.entity.Farm;
import com.backend.entity.InventoryItem;
import com.backend.repository.FarmRepository;
import com.backend.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryItemService {

    private final InventoryItemRepository inventoryItemRepository;
    private final FarmRepository farmRepository;

    public InventoryItemResponse createItem(
            Long farmId,
            InventoryItemRequest request,
            Long ownerId) {

        Farm farm = getOwnedFarm(farmId, ownerId);

        String itemName = request.getName().trim();

        if (inventoryItemRepository.existsByNameIgnoreCaseAndFarmId(
                itemName, farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An inventory item with this name already exists in this farm"
            );
        }

        InventoryItem item = InventoryItem.builder()
                .farm(farm)
                .name(itemName)
                .category(request.getCategory())
                .quantity(request.getQuantity())
                .unit(request.getUnit().trim())
                .minimum(request.getMinimum())
                .build();

        return toResponse(inventoryItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getFarmItems(
            Long farmId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return inventoryItemRepository.findByFarmId(farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getItemById(
            Long farmId,
            Long itemId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return toResponse(getFarmItem(itemId, farmId));
    }

    public InventoryItemResponse updateItem(
            Long farmId,
            Long itemId,
            InventoryItemRequest request,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        InventoryItem item = getFarmItem(itemId, farmId);

        String itemName = request.getName().trim();

        boolean duplicateExists =
                inventoryItemRepository.existsByNameIgnoreCaseAndFarmId(
                        itemName, farmId);

        if (duplicateExists && !item.getName().equalsIgnoreCase(itemName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An inventory item with this name already exists in this farm"
            );
        }

        item.setName(itemName);
        item.setCategory(request.getCategory());
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit().trim());
        item.setMinimum(request.getMinimum());

        return toResponse(inventoryItemRepository.save(item));
    }

    public void deleteItem(
            Long farmId,
            Long itemId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        InventoryItem item = getFarmItem(itemId, farmId);

        inventoryItemRepository.delete(item);
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {
        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found"
                ));
    }

    private InventoryItem getFarmItem(Long itemId, Long farmId) {
        return inventoryItemRepository.findByIdAndFarmId(itemId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inventory item not found"
                ));
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .farmId(item.getFarm().getId())
                .name(item.getName())
                .category(item.getCategory())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .minimum(item.getMinimum())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
