package com.backend.repository;

import com.backend.entity.FarmDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmDocumentRepository
        extends JpaRepository<FarmDocument, Long> {

    List<FarmDocument> findByFarmIdOrderByUploadedAtDesc(Long farmId);
    boolean existsByFarmId(Long farmId);

    Optional<FarmDocument> findByIdAndFarmId(Long id, Long farmId);
}