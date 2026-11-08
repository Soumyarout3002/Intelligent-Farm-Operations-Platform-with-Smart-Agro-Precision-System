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
public class DailyForecastResponse {

    private String date;

    private Double maxTemperatureC;
    private Double minTemperatureC;

    private Double precipitationMm;
    private Integer precipitationProbabilityMax;

    private Integer weatherCode;

    private String sunrise;
    private String sunset;

    private String description;
}
