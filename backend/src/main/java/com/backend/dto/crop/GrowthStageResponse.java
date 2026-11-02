package com.backend.dto.crop;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GrowthStageResponse {

    private Long id;
    private Long cropCycleId;
    private String stageName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String observations;
    private LocalDateTime createdAt;
}