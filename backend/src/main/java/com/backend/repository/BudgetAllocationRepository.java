
package com.backend.repository;

import com.backend.entity.BudgetAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetAllocationRepository
        extends JpaRepository<BudgetAllocation, Long> {

    // Get all category allocations of a budget
    List<BudgetAllocation> findBySeasonBudgetId(Long budgetId);

    // Find a category allocation inside a budget
    Optional<BudgetAllocation> findByIdAndSeasonBudgetId(
            Long id,
            Long budgetId
    );

    // Check whether category already exists in a budget
    boolean existsBySeasonBudgetIdAndCategoryIgnoreCase(
            Long budgetId,
            String category
    );
}
