
package com.backend.repository;

import com.backend.entity.CropActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CropActivityRepository
        extends JpaRepository<CropActivity, Long> {

    List<CropActivity> findByCropCycleIdOrderByActivityDateDesc(
            Long cropCycleId);

    Optional<CropActivity> findByIdAndCropCycleId(
            Long id, Long cropCycleId);

    @Query("""
            SELECT COALESCE(SUM(COALESCE(ca.activityCost, 0)), 0)
            FROM CropActivity ca
            WHERE ca.cropCycle.id = :cropCycleId
            """)
    BigDecimal sumActivityCostByCropCycleId(
            @Param("cropCycleId") Long cropCycleId);
}
