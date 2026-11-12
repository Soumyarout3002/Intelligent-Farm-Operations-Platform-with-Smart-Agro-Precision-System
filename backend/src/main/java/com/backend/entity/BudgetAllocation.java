
package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "budget_allocations",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_budget_category",
            columnNames = {"budget_id", "category"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "budget_id", nullable = false)
    private SeasonBudget seasonBudget;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(
        name = "allocated_amount",
        nullable = false,
        precision = 14,
        scale = 2
    )
    private BigDecimal allocatedAmount;

    @Column(length = 300)
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
