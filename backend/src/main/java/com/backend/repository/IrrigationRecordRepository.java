package com.backend.repository;

import com.backend.entity.IrrigationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IrrigationRecordRepository
        extends JpaRepository<IrrigationRecord, Long> {

    List<IrrigationRecord> findByScheduleIdOrderByRecordedAtDesc(
            Long scheduleId);
}
