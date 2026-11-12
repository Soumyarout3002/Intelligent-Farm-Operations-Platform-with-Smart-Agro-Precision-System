
package com.backend.repository;

import com.backend.entity.EquipmentBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentBreakdownRepository
        extends JpaRepository<EquipmentBreakdown, Long> {

    @Query("""
        SELECT b
        FROM EquipmentBreakdown b
        JOIN FETCH b.equipment
        JOIN FETCH b.reportedBy
        LEFT JOIN FETCH b.resolvedBy
        WHERE b.equipment.farm.id = :farmId
        ORDER BY b.reportedAt DESC
    """)
    List<EquipmentBreakdown> findBreakdownsByFarmId(
            @Param("farmId") Long farmId
    );

    @Query("""
        SELECT b
        FROM EquipmentBreakdown b
        JOIN FETCH b.equipment
        JOIN FETCH b.reportedBy
        LEFT JOIN FETCH b.resolvedBy
        WHERE b.id = :breakdownId
          AND b.equipment.farm.id = :farmId
    """)
    Optional<EquipmentBreakdown> findBreakdownByIdAndFarmId(
            @Param("breakdownId") Long breakdownId,
            @Param("farmId") Long farmId
    );
    @Query("""
    SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
    FROM EquipmentBreakdown b
    WHERE b.equipment.id = :equipmentId
      AND UPPER(b.status) <> 'RESOLVED'
""")
    boolean existsActiveBreakdown(
            @Param("equipmentId") Long equipmentId);

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM EquipmentBreakdown b
        WHERE b.equipment.id = :equipmentId
        AND b.id <> :currentBreakdownId
        AND UPPER(b.status) <> 'RESOLVED'
    """)
    boolean existsOtherActiveBreakdown(
            @Param("equipmentId") Long equipmentId,
            @Param("currentBreakdownId") Long currentBreakdownId);
    boolean existsByEquipment_Id(Long equipmentId);
}
