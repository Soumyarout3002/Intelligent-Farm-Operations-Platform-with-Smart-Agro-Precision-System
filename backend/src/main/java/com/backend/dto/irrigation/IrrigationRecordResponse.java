package com.backend.dto.irrigation;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrrigationRecordResponse {

    private Long id;

    private Long scheduleId;

    private String action;

    private String notes;

    private LocalDateTime recordedAt;

}    