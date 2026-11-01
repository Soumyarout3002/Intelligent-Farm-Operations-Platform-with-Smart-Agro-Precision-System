package com.backend.dto.farm;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmDocumentResponse {

    private Long id;
    private Long farmId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String description;
    private LocalDateTime uploadedAt;
}