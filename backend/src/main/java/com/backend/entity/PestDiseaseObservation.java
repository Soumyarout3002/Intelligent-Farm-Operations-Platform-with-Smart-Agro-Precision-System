package com.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pest_disease_observations")
@Getter
@Setter
@NoArgsConstructor
public class PestDiseaseObservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String issueType;

    @Column(nullable = false, length = 150)
    private String issueName;

    @Column(nullable = false)
    private LocalDate observationDate;

    @Column(nullable = false, length = 30)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String symptoms;

    private String affectedArea;

    @Column(columnDefinition = "TEXT")
    private String actionTaken;

    private String treatment;

    private LocalDate followUpDate;

    @Column(nullable = false, length = 30)
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_cycle_id", nullable = false)
    private CropCycle cropCycle;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
