
package com.backend.dto.inventory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemResponse {

    private Long id;

    private Long farmId;

    private String name;

    private String category;

    private BigDecimal quantity;

    private String unit;

    private BigDecimal minimum;

    private LocalDate expiryDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
