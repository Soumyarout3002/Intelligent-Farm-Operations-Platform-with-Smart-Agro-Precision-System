package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "irrigation_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrrigationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private IrrigationSchedule schedule;

    @Column(nullable = false)
    private String action;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
