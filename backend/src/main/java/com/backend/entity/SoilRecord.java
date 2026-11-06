
package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "soil_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoilRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plot_id", nullable = false)
    private Plot plot;

    @Column(precision = 4, scale = 2)
    private BigDecimal phLevel;

    @Column(precision = 10, scale = 2)
    private BigDecimal nitrogen;

    @Column(precision = 10, scale = 2)
    private BigDecimal phosphorus;

    @Column(precision = 10, scale = 2)
    private BigDecimal potassium;

    @Column(precision = 6, scale = 2)
    private BigDecimal organicMatter;

    @Column(precision = 6, scale = 2)
    private BigDecimal moisture;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }
}