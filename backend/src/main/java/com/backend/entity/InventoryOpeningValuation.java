
package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "inventory_opening_valuations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_opening_valuation_item_warehouse",
            columnNames = {"inventory_item_id", "warehouse_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryOpeningValuation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal openingQuantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal openingUnitCost;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal openingTotalCost;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
