
package com.backend.repository;

import com.backend.entity.EquipmentUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentUsageRepository
        extends JpaRepository<EquipmentUsage, Long> {

    List<EquipmentUsage> findByEquipmentFarmIdOrderByUsageDateDesc(Long farmId);

    List<EquipmentUsage> findByEquipmentIdAndEquipmentFarmIdOrderByUsageDateDesc(
            Long equipmentId, Long farmId
    );

    boolean existsByEquipment_Id(Long equipmentId);


    @Query("""
        SELECT COALESCE(
            SUM(COALESCE(eu.fuelCost, 0) + COALESCE(eu.usageCost, 0)),
            0
        )
        FROM EquipmentUsage eu
        WHERE eu.cropCycle.id = :cropCycleId
        """)
    BigDecimal sumTotalCostByCropCycleId(
            @Param("cropCycleId") Long cropCycleId
    );

}
