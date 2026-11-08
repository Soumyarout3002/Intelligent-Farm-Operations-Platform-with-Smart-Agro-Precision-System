package com.backend.dto.irrigation;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrrigationScheduleResponse {

    private Long id;

    private Long farmId;

    private String farmName;

    private LocalDateTime scheduledAt;

    private Double waterAmountLiters;

    private String notes;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}