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
public class CurrentWeatherResponse {

    private Long farmId;
    private String farmName;

    private Double latitude;
    private Double longitude;

    private Double temperatureC;
    private Integer relativeHumidityPercent;
    private Double apparentTemperatureC;

    private Boolean daytime;

    private Double precipitationMm;
    private Double rainMm;

    private Integer weatherCode;
    private Double windSpeedKmh;

    private String observedAt;
    private String description;
}