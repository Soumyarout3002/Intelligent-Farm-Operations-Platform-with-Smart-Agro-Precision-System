package com.backend.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherAlertResponse {

    private Long farmId;
    private String farmName;

    private String alertType;
    private String severity;

    private String title;
    private String message;

    private String expectedDate;
}