package com.backend.dto.crop;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CropActivityResponse {

    private Long id;
    private Long cropCycleId;
    private String activityType;
    private String title;
    private LocalDate activityDate;
    private String description;
    private String quantity;
    private String performedBy;
    private LocalDateTime createdAt;
    
}