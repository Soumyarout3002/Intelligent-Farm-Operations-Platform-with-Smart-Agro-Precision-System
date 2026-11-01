package com.backend.controller;

import com.backend.dto.farm.FarmDocumentResponse;
import com.backend.entity.User;
import com.backend.service.FarmDocumentService;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/farms/{farmId}/documents")
@RequiredArgsConstructor
public class FarmDocumentController {

    private final FarmDocumentService documentService;

    private Long getOwnerId(HttpServletRequest request) {

        Object authenticatedUser =
                request.getAttribute("authenticatedUser");

        if (!(authenticatedUser instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        return user.getId();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FarmDocumentResponse> uploadDocument(
            @PathVariable Long farmId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false)
                    String description,
            HttpServletRequest httpRequest) {

        FarmDocumentResponse response =
                documentService.uploadDocument(
                        farmId, getOwnerId(httpRequest), file, description);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FarmDocumentResponse>> getFarmDocuments(
            @PathVariable Long farmId,
            HttpServletRequest httpRequest) {

        return ResponseEntity.ok(
                documentService.getFarmDocuments(
                        farmId, getOwnerId(httpRequest)));
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long farmId,
            @PathVariable Long documentId,
            HttpServletRequest httpRequest) {

        Resource resource = documentService.downloadDocument(
                farmId, documentId, getOwnerId(httpRequest));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable Long farmId,
            @PathVariable Long documentId,
            HttpServletRequest httpRequest) {

        documentService.deleteDocument(
                farmId, documentId, getOwnerId(httpRequest));

        return ResponseEntity.noContent().build();
    }
}