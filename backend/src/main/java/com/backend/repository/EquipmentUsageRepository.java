
package com.backend.repository;

import com.backend.entity.EquipmentUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentUsageRepository
        extends JpaRepository<EquipmentUsage, Long> {

    List<EquipmentUsage> findByEquipmentFarmIdOrderByUsageDateDesc(Long farmId);

    List<EquipmentUsage> findByEquipmentIdAndEquipmentFarmIdOrderByUsageDateDesc(
            Long equipmentId, Long farmId
    );

    boolean existsByEquipment_Id(Long equipmentId);
}
