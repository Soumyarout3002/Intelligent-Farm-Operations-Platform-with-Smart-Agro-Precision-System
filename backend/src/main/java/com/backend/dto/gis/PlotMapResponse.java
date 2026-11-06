package com.backend.dto.gis;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlotMapResponse {

    private Long plotId;
    private String plotName;
    private String plotCode;

    private BigDecimal area;
    private String areaUnit;
    private String status;

    private String boundaryGeoJson;

    private String cropName;
    private String description;
}
