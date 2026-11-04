package com.backend.service;

import com.backend.dto.farm.FarmDocumentResponse;
import com.backend.entity.Farm;
import com.backend.entity.FarmDocument;
import com.backend.repository.FarmDocumentRepository;
import com.backend.repository.FarmRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmDocumentService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("pdf", "jpg", "jpeg", "png", "doc", "docx");

    private final FarmRepository farmRepository;
    private final FarmDocumentRepository documentRepository;
    private final FarmAccessService farmAccessService;

    @Value("${app.upload.dir:uploads/farm-documents}")
    private String uploadDirectory;

    public FarmDocumentResponse uploadDocument(
            Long farmId,
            Long ownerId,
            MultipartFile file,
            String description) {

        Farm farm = farmAccessService.requireManageable(farmId, ownerId);

        validateFile(file);

        String originalName = Paths.get(
                file.getOriginalFilename() == null
                        ? "document"
                        : file.getOriginalFilename()
        ).getFileName().toString();

        String extension = getExtension(originalName);
        String storageName = UUID.randomUUID() + "." + extension;

        Path uploadPath = Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        Path destination = uploadPath.resolve(storageName).normalize();

        if (!destination.startsWith(uploadPath)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file path");
        }

        try {
            Files.createDirectories(uploadPath);
           Files.copy(file.getInputStream(), destination);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to store uploaded document");
        }

        try {
            FarmDocument document = FarmDocument.builder()
                    .farm(farm)
                    .fileName(originalName)
                    .contentType(file.getContentType() == null
                            ? "application/octet-stream"
                            : file.getContentType())
                    .fileSize(file.getSize())
                    .storageName(storageName)
                    .description(description)
                    .build();

            return mapToResponse(documentRepository.save(document));

        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {
                // Database failure should not hide the original exception.
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<FarmDocumentResponse> getFarmDocuments(
            Long farmId, Long ownerId) {

        farmAccessService.requireManageable(farmId, ownerId);

        return documentRepository
                .findByFarmIdOrderByUploadedAtDesc(farmId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadDocument(
            Long farmId,
            Long documentId,
            Long ownerId) {

        farmAccessService.requireManageable(farmId, ownerId);

        FarmDocument document = findDocument(farmId, documentId);

        Path filePath = getUploadPath()
                .resolve(document.getStorageName())
                .normalize();

        if (!filePath.startsWith(getUploadPath())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid document path");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Document file not found");
            }

            return resource;

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read document");
        }
    }

    public void deleteDocument(
            Long farmId,
            Long documentId,
            Long ownerId) {

        farmAccessService.requireManageable(farmId, ownerId);

        FarmDocument document = findDocument(farmId, documentId);

        Path filePath = getUploadPath()
                .resolve(document.getStorageName())
                .normalize();

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to delete document file");
        }

        documentRepository.delete(document);
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please select a file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File size must not exceed 10 MB");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        String extension = getExtension(originalName);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Allowed file types: PDF, JPG, JPEG, PNG, DOC, DOCX");
        }
    }

    private String getExtension(String fileName) {

        int dotIndex = fileName.lastIndexOf('.');

        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "File extension is required");
        }

        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private Path getUploadPath() {
        return Paths.get(uploadDirectory)
                .toAbsolutePath()
                .normalize();
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {

        return farmAccessService.requireReadable(farmId, ownerId);
    }

    private FarmDocument findDocument(Long farmId, Long documentId) {

        return documentRepository.findByIdAndFarmId(documentId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Document not found"));
    }

    private FarmDocumentResponse mapToResponse(FarmDocument document) {

        return FarmDocumentResponse.builder()
                .id(document.getId())
                .farmId(document.getFarm().getId())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .description(document.getDescription())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}