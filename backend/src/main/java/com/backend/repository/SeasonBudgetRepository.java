package com.backend.repository;

import com.backend.entity.SeasonBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonBudgetRepository
        extends JpaRepository<SeasonBudget, Long> {

    // Find budget associated with a crop cycle
    Optional<SeasonBudget> findByCropCycleId(Long cropCycleId);

    // Check whether a crop cycle already has a budget
    boolean existsByCropCycleId(Long cropCycleId);
}
