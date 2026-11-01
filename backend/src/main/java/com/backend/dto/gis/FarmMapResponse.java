package com.backend.dto.gis;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmMapResponse {

    private Long farmId;
    private String farmName;
    private String location;
    private BigDecimal farmArea;
    private String farmAreaUnit;
    private String primaryCrop;
    private String farmStatus;

    private List<PlotMapResponse> plots;
}
