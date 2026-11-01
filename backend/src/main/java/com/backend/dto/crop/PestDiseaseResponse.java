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
public class PestDiseaseResponse {

    private Long id;
    private Long cropCycleId;
    private String issueType;
    private String issueName;
    private LocalDate observationDate;
    private String severity;
    private String symptoms;
    private String affectedArea;
    private String actionTaken;
    private String treatment;
    private LocalDate followUpDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}