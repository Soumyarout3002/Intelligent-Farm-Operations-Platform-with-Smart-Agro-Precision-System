package com.backend.dto.farm;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class FarmResponse {
    private Long id;
    private String name;
    private String location;
    private BigDecimal area;
    private String areaUnit;
    private String soilType;
    private String primaryCrop;
    private String status;
    private String notes;
    private Double latitude;
    private Double longitude;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
