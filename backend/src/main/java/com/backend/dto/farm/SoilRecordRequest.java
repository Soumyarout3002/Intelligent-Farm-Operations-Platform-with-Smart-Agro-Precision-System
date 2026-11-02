package com.backend.dto.farm;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoilRecordRequest {

    @DecimalMin("0.0")
    @DecimalMax("14.0")
    private BigDecimal phLevel;

    @DecimalMin("0.0")
    private BigDecimal nitrogen;

    @DecimalMin("0.0")
    private BigDecimal phosphorus;

    @DecimalMin("0.0")
    private BigDecimal potassium;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal organicMatter;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal moisture;

    private LocalDateTime recordedAt;

    @Size(max = 2000)
    private String notes;
}