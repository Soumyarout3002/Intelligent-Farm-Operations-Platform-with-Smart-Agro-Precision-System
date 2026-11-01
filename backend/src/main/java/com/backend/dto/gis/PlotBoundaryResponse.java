package com.backend.dto.gis;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlotBoundaryResponse {

    private Long plotId;
    private String plotName;

    private String boundaryGeoJson;

    private BigDecimal area;
    private String areaUnit;
}