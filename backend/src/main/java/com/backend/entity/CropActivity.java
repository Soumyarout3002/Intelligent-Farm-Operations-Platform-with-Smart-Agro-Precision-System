package com.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "crop_activities")
@Getter
@Setter
@NoArgsConstructor
public class CropActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String activityType;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String quantity;

    private String performedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_cycle_id", nullable = false)
    private CropCycle cropCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @Column(precision = 12, scale = 2)
    private BigDecimal usedQuantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal activityCost;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
