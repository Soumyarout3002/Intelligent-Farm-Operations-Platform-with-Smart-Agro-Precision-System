
package com.backend.service;

import com.backend.dto.inventory.InventoryAlertResponse;
import com.backend.entity.Farm;
import com.backend.entity.InventoryItem;
import com.backend.repository.FarmRepository;
import com.backend.repository.InventoryItemRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryAlertService {
        
        private final InventoryItemRepository inventoryItemRepository;
        private final FarmRepository farmRepository;
        private final NotificationService notificationService;
    @Transactional
    public List<InventoryAlertResponse> getFarmAlerts(
            Long farmId,
            Long ownerId,
            int expiryDays) {

        // Validate expiry window
        if (expiryDays < 0 || expiryDays > 365) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiry days must be between 0 and 365"
            );
        }

        Farm farm = farmRepository.findByIdAndOwnerId(farmId, ownerId)
        .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Farm not found"
        ));

        List<InventoryAlertResponse> alerts = new ArrayList<>();

        // 1. Low-stock alerts
        List<InventoryItem> lowStockItems =
                inventoryItemRepository.findLowStockItems(farmId);

        for (InventoryItem item : lowStockItems) {
            alerts.add(InventoryAlertResponse.builder()
                    .itemId(item.getId())
                    .itemName(item.getName())
                    .category(item.getCategory())
                    .currentQuantity(item.getQuantity())
                    .unit(item.getUnit())
                    .minimumQuantity(item.getMinimum())
                    .expiryDate(item.getExpiryDate())
                    .alertType("LOW_STOCK")
                    .message(
                            item.getName() + " stock is low. Available: "
                                    + item.getQuantity() + " " + item.getUnit()
                                    + ", minimum required: "
                                    + item.getMinimum() + " " + item.getUnit()
                    )
                    .build());
        }

        // 2. Expiry alerts
        LocalDate today = LocalDate.now();
        LocalDate expiryLimit = today.plusDays(expiryDays);

        List<InventoryItem> expiryItems =
                inventoryItemRepository.findItemsExpiringBefore(
                        farmId, expiryLimit
                );

        for (InventoryItem item : expiryItems) {

            boolean expired = item.getExpiryDate().isBefore(today);

            String alertType = expired ? "EXPIRED" : "EXPIRING_SOON";

            String message = expired
                    ? item.getName() + " has expired on " + item.getExpiryDate()
                    : item.getName() + " will expire on " + item.getExpiryDate();

            alerts.add(InventoryAlertResponse.builder()
                    .itemId(item.getId())
                    .itemName(item.getName())
                    .category(item.getCategory())
                    .currentQuantity(item.getQuantity())
                    .unit(item.getUnit())
                    .minimumQuantity(item.getMinimum())
                    .expiryDate(item.getExpiryDate())
                    .alertType(alertType)
                    .message(message)
                    .build());
        }
        for (InventoryAlertResponse alert : alerts) {

    String title;
    String type;

    switch (alert.getAlertType()) {
        case "LOW_STOCK" -> {
            title = "Low stock alert";
            type = "INVENTORY";
        }
        case "EXPIRED" -> {
            title = "Inventory item expired";
            type = "INVENTORY";
        }
        default -> {
            title = "Inventory expiry reminder";
            type = "INVENTORY";
        }
    }

    String dedupeKey = "INVENTORY:"
            + alert.getItemId()
            + ":"
            + alert.getAlertType();

    notificationService.createNotification(
            farm.getOwner().getId(),
            farmId,
            title,
            alert.getMessage(),
            type,
            dedupeKey
    );
}


        return alerts;
    }
}
