package com.backend.dto.crop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class PestDiseaseRequest {

    @NotBlank(message = "Issue type is required")
    @Size(max = 20)
    private String issueType;

    @NotBlank(message = "Issue name is required")
    @Size(max = 150)
    private String issueName;

    @NotNull(message = "Observation date is required")
    private LocalDate observationDate;

    @NotBlank(message = "Severity is required")
    private String severity;

    private String symptoms;

    private String affectedArea;

    private String actionTaken;

    private String treatment;

    private LocalDate followUpDate;

    private String status;
}