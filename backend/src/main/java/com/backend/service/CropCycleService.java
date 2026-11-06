package com.backend.service;

import com.backend.dto.crop.CropCycleRequest;
import com.backend.dto.crop.CropCycleResponse;
import com.backend.entity.CropCycle;
import com.backend.entity.Farm;
import com.backend.entity.Plot;
import com.backend.repository.CropCycleRepository;
import com.backend.repository.FarmRepository;
import com.backend.repository.PlotRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class CropCycleService {

    private final CropCycleRepository cropCycleRepository;
    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;

    private static final Set<String> VALID_STATUSES =
            Set.of("PLANNED", "ACTIVE", "COMPLETED", "CANCELLED");

    public CropCycleService(
            CropCycleRepository cropCycleRepository,
            FarmRepository farmRepository,
            PlotRepository plotRepository) {

        this.cropCycleRepository = cropCycleRepository;
        this.farmRepository = farmRepository;
        this.plotRepository = plotRepository;
    }

    // Create crop cycle
    @Transactional
    public CropCycleResponse createCropCycle(
            Long farmId,
            Long plotId,
            Long ownerId,
            CropCycleRequest request) {

        Farm farm = getOwnedFarm(farmId, ownerId);
        Plot plot = getFarmPlot(plotId, farm.getId());

        validateRequest(request);

        CropCycle cropCycle = new CropCycle();
        cropCycle.setPlot(plot);

        updateCropCycleFields(cropCycle, request);

        CropCycle saved = cropCycleRepository.save(cropCycle);

        return mapToResponse(saved);
    }

    // Get all crop cycles for a plot
    @Transactional(readOnly = true)
    public List<CropCycleResponse> getCropCycles(
            Long farmId,
            Long plotId,
            Long ownerId) {

        Farm farm = getOwnedFarm(farmId, ownerId);
        getFarmPlot(plotId, farm.getId());

        return cropCycleRepository
                .findByPlotIdOrderByCreatedAtDesc(plotId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get one crop cycle
    @Transactional(readOnly = true)
    public CropCycleResponse getCropCycle(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long ownerId) {

        Farm farm = getOwnedFarm(farmId, ownerId);
        getFarmPlot(plotId, farm.getId());

        CropCycle cropCycle = getExistingCycle(cycleId, plotId);

        return mapToResponse(cropCycle);
    }

    // Update crop cycle
    @Transactional
    public CropCycleResponse updateCropCycle(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long ownerId,
            CropCycleRequest request) {

        Farm farm = getOwnedFarm(farmId, ownerId);
        getFarmPlot(plotId, farm.getId());

        validateRequest(request);

        CropCycle cropCycle = getExistingCycle(cycleId, plotId);

        updateCropCycleFields(cropCycle, request);

        CropCycle updated = cropCycleRepository.save(cropCycle);

        return mapToResponse(updated);
    }

    // Delete crop cycle
    @Transactional
    public void deleteCropCycle(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long ownerId) {

        Farm farm = getOwnedFarm(farmId, ownerId);
        getFarmPlot(plotId, farm.getId());

        CropCycle cropCycle = getExistingCycle(cycleId, plotId);

        cropCycleRepository.delete(cropCycle);
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {

        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Farm not found"
                        ));
    }

    private Plot getFarmPlot(Long plotId, Long farmId) {

        return plotRepository.findByIdAndFarmId(plotId, farmId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Plot not found"
                        ));
    }

    private CropCycle getExistingCycle(Long cycleId, Long plotId) {

        return cropCycleRepository.findByIdAndPlotId(cycleId, plotId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Crop cycle not found"
                        ));
    }

    private void validateRequest(CropCycleRequest request) {

    if (request.getExpectedHarvestDate() != null
            && request.getExpectedHarvestDate()
            .isBefore(request.getSowingDate())) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Expected harvest date cannot be before sowing date"
        );
    }

    String status = request.getStatus() == null
            || request.getStatus().isBlank()
            ? "PLANNED"
            : request.getStatus().trim().toUpperCase();

    if (!VALID_STATUSES.contains(status)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid crop cycle status. Allowed: PLANNED, ACTIVE, COMPLETED, CANCELLED"
        );
    }
}

    private void updateCropCycleFields(
        CropCycle cropCycle,
        CropCycleRequest request) {

    cropCycle.setCropName(request.getCropName().trim());
    cropCycle.setVariety(request.getVariety());
    cropCycle.setSeason(request.getSeason());
    cropCycle.setSowingDate(request.getSowingDate());
    cropCycle.setExpectedHarvestDate(request.getExpectedHarvestDate());

    String status = request.getStatus() == null
            || request.getStatus().isBlank()
            ? "PLANNED"
            : request.getStatus().trim().toUpperCase();

    cropCycle.setStatus(status);
    cropCycle.setNotes(request.getNotes());
}
    private CropCycleResponse mapToResponse(CropCycle cropCycle) {

        return new CropCycleResponse(
                cropCycle.getId(),
                cropCycle.getPlot().getId(),
                cropCycle.getPlot().getName(),
                cropCycle.getCropName(),
                cropCycle.getVariety(),
                cropCycle.getSeason(),
                cropCycle.getSowingDate(),
                cropCycle.getExpectedHarvestDate(),
                cropCycle.getActualHarvestDate(),
                cropCycle.getStatus(),
                cropCycle.getNotes(),
                cropCycle.getCreatedAt(),
                cropCycle.getUpdatedAt()
        );
    }
}
