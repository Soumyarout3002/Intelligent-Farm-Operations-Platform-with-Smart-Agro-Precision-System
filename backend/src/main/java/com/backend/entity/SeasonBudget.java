
package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "season_budgets",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_season_budget_crop_cycle",
            columnNames = "crop_cycle_id"
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_cycle_id", nullable = false, unique = true)
    private CropCycle cropCycle;

    @Column(
        name = "total_budget",
        nullable = false,
        precision = 14,
        scale = 2
    )
    private BigDecimal totalBudget;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
