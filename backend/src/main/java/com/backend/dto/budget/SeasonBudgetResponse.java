
package com.backend.dto.budget;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class SeasonBudgetResponse {

    private Long id;

    private Long cropCycleId;

    private String cropName;

    private String season;

    private BigDecimal totalBudget;

    private BigDecimal totalAllocated;

    private BigDecimal actualExpense;

    private Boolean alert;

    // Budget not yet assigned to any category
    private BigDecimal unallocatedBudget;

    // Total budget remaining after actual expenses
    private BigDecimal remainingBudget;

    private List<BudgetCategoryResponse> allocations;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
