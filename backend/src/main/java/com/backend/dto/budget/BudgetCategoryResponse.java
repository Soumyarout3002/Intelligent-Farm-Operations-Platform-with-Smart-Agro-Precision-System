package com.backend.dto.budget;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BudgetCategoryResponse {

    private Long id;

    private String category;

    private BigDecimal allocatedAmount;

    private BigDecimal actualExpense;

    private BigDecimal remainingAmount;

    private BigDecimal utilizationPercentage;

    private Boolean alert;

    private String notes;
}
