package com.backend.service;

import com.backend.dto.crop.PestDiseaseRequest;
import com.backend.dto.crop.PestDiseaseResponse;
import com.backend.entity.CropCycle;
import com.backend.entity.Farm;
import com.backend.entity.PestDiseaseObservation;
import com.backend.repository.CropCycleRepository;
import com.backend.repository.FarmRepository;
import com.backend.repository.PestDiseaseObservationRepository;
import com.backend.repository.PlotRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class PestDiseaseService {

    private final PestDiseaseObservationRepository observationRepository;
    private final CropCycleRepository cropCycleRepository;
    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;

    private static final Set<String> VALID_ISSUE_TYPES =
            Set.of("PEST", "DISEASE");

    private static final Set<String> VALID_SEVERITIES =
            Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL");

    private static final Set<String> VALID_STATUSES =
            Set.of("OPEN", "UNDER_TREATMENT", "RESOLVED");

    public PestDiseaseService(
            PestDiseaseObservationRepository observationRepository,
            CropCycleRepository cropCycleRepository,
            FarmRepository farmRepository,
            PlotRepository plotRepository) {

        this.observationRepository = observationRepository;
        this.cropCycleRepository = cropCycleRepository;
        this.farmRepository = farmRepository;
        this.plotRepository = plotRepository;
    }

    // Create observation
    @Transactional
    public PestDiseaseResponse createObservation(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long ownerId,
            PestDiseaseRequest request) {

        CropCycle cycle = getOwnedCycle(
                farmId, plotId, cycleId, ownerId);

        validateRequest(request);

        PestDiseaseObservation observation =
                new PestDiseaseObservation();

        observation.setCropCycle(cycle);
        updateFields(observation, request);

        return mapToResponse(
                observationRepository.save(observation));
    }

    // Get all observations
    @Transactional(readOnly = true)
    public List<PestDiseaseResponse> getObservations(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        return observationRepository
                .findByCropCycleIdOrderByObservationDateDesc(cycleId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get one observation
    @Transactional(readOnly = true)
    public PestDiseaseResponse getObservation(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long observationId,
            Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        return mapToResponse(getExistingObservation(
                observationId, cycleId));
    }

    // Update observation
    @Transactional
    public PestDiseaseResponse updateObservation(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long observationId,
            Long ownerId,
            PestDiseaseRequest request) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        validateRequest(request);

        PestDiseaseObservation observation =
                getExistingObservation(observationId, cycleId);

        updateFields(observation, request);

        return mapToResponse(
                observationRepository.save(observation));
    }

    // Delete observation
    @Transactional
    public void deleteObservation(Long farmId,Long plotId,Long cycleId,Long observationId,Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        PestDiseaseObservation observation =
                getExistingObservation(observationId, cycleId);

        observationRepository.delete(observation);
    }

    private CropCycle getOwnedCycle(Long farmId,Long plotId,Long cycleId,Long ownerId) {

        Farm farm = farmRepository
                .findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        plotRepository.findByIdAndFarmId(plotId, farm.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plot not found"));

        return cropCycleRepository.findByIdAndPlotId(cycleId, plotId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crop cycle not found"));
    }

    private PestDiseaseObservation getExistingObservation(
            Long observationId, Long cycleId) {

        return observationRepository
                .findByIdAndCropCycleId(observationId, cycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pest/disease observation not found"));
    }

    private void validateRequest(PestDiseaseRequest request) {

    String issueType = request.getIssueType()
            .trim().toUpperCase();

    String severity = request.getSeverity()
            .trim().toUpperCase();

    String status = request.getStatus() == null
            || request.getStatus().isBlank()
            ? "OPEN"
            : request.getStatus().trim().toUpperCase();

    if (!VALID_ISSUE_TYPES.contains(issueType)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Issue type must be PEST or DISEASE");
    }

    if (!VALID_SEVERITIES.contains(severity)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Severity must be LOW, MEDIUM, HIGH or CRITICAL");
    }

    if (!VALID_STATUSES.contains(status)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Status must be OPEN, UNDER_TREATMENT or RESOLVED");
    }

    if (request.getFollowUpDate() != null
            && request.getFollowUpDate()
            .isBefore(request.getObservationDate())) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Follow-up date cannot be before observation date");
    }
}

    private void updateFields(
        PestDiseaseObservation observation,
        PestDiseaseRequest request) {

    observation.setIssueType(
            request.getIssueType().trim().toUpperCase());

    observation.setIssueName(request.getIssueName().trim());

    observation.setObservationDate(request.getObservationDate());

    observation.setSeverity(
            request.getSeverity().trim().toUpperCase());

    observation.setSymptoms(request.getSymptoms());
    observation.setAffectedArea(request.getAffectedArea());
    observation.setActionTaken(request.getActionTaken());
    observation.setTreatment(request.getTreatment());
    observation.setFollowUpDate(request.getFollowUpDate());

    String status = request.getStatus() == null
            || request.getStatus().isBlank()
            ? "OPEN"
            : request.getStatus().trim().toUpperCase();

    observation.setStatus(status);
}
    private PestDiseaseResponse mapToResponse(
            PestDiseaseObservation observation) {

        return new PestDiseaseResponse(
                observation.getId(),
                observation.getCropCycle().getId(),
                observation.getIssueType(),
                observation.getIssueName(),
                observation.getObservationDate(),
                observation.getSeverity(),
                observation.getSymptoms(),
                observation.getAffectedArea(),
                observation.getActionTaken(),
                observation.getTreatment(),
                observation.getFollowUpDate(),
                observation.getStatus(),
                observation.getCreatedAt(),
                observation.getUpdatedAt()
        );
    }
}
