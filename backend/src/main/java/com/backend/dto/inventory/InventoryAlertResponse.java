
package com.backend.dto.inventory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryAlertResponse {

    private Long itemId;

    private String itemName;

    private String category;

    private BigDecimal currentQuantity;

    private String unit;

    private BigDecimal minimumQuantity;

    private LocalDate expiryDate;

    private String alertType;

    private String message;
}
