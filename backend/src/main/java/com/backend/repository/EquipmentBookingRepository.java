package com.backend.repository;

import com.backend.entity.EquipmentBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EquipmentBookingRepository
        extends JpaRepository<EquipmentBooking, Long> {

    @EntityGraph(attributePaths = {
            "equipment",
            "farmer",
            "reviewedBy"
    })
    List<EquipmentBooking> findByEquipmentFarmIdOrderByBookingDateAsc(
            Long farmId
    );

    @EntityGraph(attributePaths = {
            "equipment",
            "farmer",
            "reviewedBy"
    })
    Optional<EquipmentBooking> findByIdAndEquipmentFarmId(
            Long bookingId,
            Long farmId
    );

    boolean existsByEquipmentIdAndBookingDateAndStatusIgnoreCase(
            Long equipmentId,
            LocalDate bookingDate,
            String status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT e
           FROM Equipment e
           WHERE e.id = :equipmentId
             AND e.farm.id = :farmId
           """)
    Optional<com.backend.entity.Equipment> findEquipmentForUpdate(
            @Param("equipmentId") Long equipmentId,
            @Param("farmId") Long farmId
    );

    boolean existsByEquipmentIdAndBookingDateAndStatusIgnoreCaseAndIdNot(
            Long equipmentId,
            LocalDate bookingDate,
            String status,
            Long bookingId
    );
    boolean existsByEquipment_Id(Long equipmentId);
}