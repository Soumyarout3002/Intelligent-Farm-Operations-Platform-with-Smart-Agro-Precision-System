package com.backend.repository;

import com.backend.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByFarmId(Long farmId);

    Optional<Equipment> findByIdAndFarmId(Long id, Long farmId);

    boolean existsByNameIgnoreCaseAndFarmId(String name, Long farmId);

    boolean existsByIdAndFarmId(Long id, Long farmId);
}