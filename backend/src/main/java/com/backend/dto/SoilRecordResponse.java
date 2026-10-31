package com.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoilRecordResponse {

    private Long id;

    private Long plotId;
    private String plotName;

    private BigDecimal phLevel;
    private BigDecimal nitrogen;
    private BigDecimal phosphorus;
    private BigDecimal potassium;
    private BigDecimal organicMatter;
    private BigDecimal moisture;

    private LocalDateTime recordedAt;
    private LocalDateTime createdAt;
    private String notes;
}