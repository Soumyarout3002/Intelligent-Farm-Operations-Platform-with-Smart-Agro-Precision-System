package com.backend.dto.farm;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlotResponse {

    private Long id;
    private String name;
    private String plotCode;
    private BigDecimal area;
    private String areaUnit;
    private String status;
    private String description;
    private Long farmId;
    private String farmName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
