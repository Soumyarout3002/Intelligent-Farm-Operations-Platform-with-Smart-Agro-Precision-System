package com.backend.repository;

import com.backend.entity.IrrigationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IrrigationScheduleRepository
        extends JpaRepository<IrrigationSchedule, Long> {

    @Query("""
        SELECT s FROM IrrigationSchedule s
        JOIN FETCH s.farm
        WHERE s.farm.id = :farmId
        ORDER BY s.scheduledAt DESC
    """)
    List<IrrigationSchedule> findSchedulesWithFarm(
            @Param("farmId") Long farmId);

    @Query("""
        SELECT s FROM IrrigationSchedule s
        JOIN FETCH s.farm
        WHERE s.id = :scheduleId
        AND s.farm.id = :farmId
    """)
    Optional<IrrigationSchedule> findScheduleWithFarm(
            @Param("scheduleId") Long scheduleId,
            @Param("farmId") Long farmId);
        }