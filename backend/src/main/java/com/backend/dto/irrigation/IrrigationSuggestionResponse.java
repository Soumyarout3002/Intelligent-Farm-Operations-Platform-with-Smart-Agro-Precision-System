package com.backend.dto.irrigation;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrrigationSuggestionResponse {

    private Long farmId;

    private String farmName;

    private String date;

    private String recommendation;

    private String reason;

    private String priority;
}