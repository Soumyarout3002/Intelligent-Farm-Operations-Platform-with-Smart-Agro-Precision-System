package com.backend.service;

import com.backend.dto.crop.HarvestRequest;
import com.backend.dto.crop.HarvestResponse;
import com.backend.entity.CropCycle;
import com.backend.entity.Farm;
import com.backend.entity.HarvestRecord;
import com.backend.repository.CropCycleRepository;
import com.backend.repository.FarmRepository;
import com.backend.repository.HarvestRecordRepository;
import com.backend.repository.PlotRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class HarvestService {

    private final HarvestRecordRepository harvestRepository;
    private final CropCycleRepository cropCycleRepository;
    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;

    private static final Set<String> VALID_UNITS =
            Set.of("KG", "TONNE", "QUINTAL", "GRAM");

    private static final Set<String> VALID_QUALITY_GRADES =
            Set.of("A", "B", "C", "PREMIUM", "STANDARD", "LOW");

    public HarvestService(
            HarvestRecordRepository harvestRepository,
            CropCycleRepository cropCycleRepository,
            FarmRepository farmRepository,
            PlotRepository plotRepository) {

        this.harvestRepository = harvestRepository;
        this.cropCycleRepository = cropCycleRepository;
        this.farmRepository = farmRepository;
        this.plotRepository = plotRepository;
    }

    // Create harvest record
    @Transactional
    public HarvestResponse createHarvest(Long farmId,Long plotId,Long cycleId,Long ownerId,HarvestRequest request) {

        CropCycle cycle = getOwnedCycle(
                farmId, plotId, cycleId, ownerId);

        validateRequest(request);

        HarvestRecord harvest = new HarvestRecord();
        harvest.setCropCycle(cycle);

        updateFields(harvest, request);

        HarvestRecord saved = harvestRepository.save(harvest);

        // Mark crop cycle as completed after recording harvest.
        cycle.setActualHarvestDate(request.getHarvestDate());
        cycle.setStatus("COMPLETED");

        cropCycleRepository.save(cycle);

        return mapToResponse(saved);
    }

    // Get all harvest records
    @Transactional(readOnly = true)
    public List<HarvestResponse> getHarvests(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        return harvestRepository
                .findByCropCycleIdOrderByHarvestDateDesc(cycleId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get one harvest record
    @Transactional(readOnly = true)
    public HarvestResponse getHarvest(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long harvestId,
            Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        return mapToResponse(
                getExistingHarvest(harvestId, cycleId));
    }

    // Update harvest record
    @Transactional
    public HarvestResponse updateHarvest(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long harvestId,
            Long ownerId,
            HarvestRequest request) {

        CropCycle cycle = getOwnedCycle(
                farmId, plotId, cycleId, ownerId);

        validateRequest(request);

        HarvestRecord harvest =
                getExistingHarvest(harvestId, cycleId);

        updateFields(harvest, request);

        HarvestRecord updated = harvestRepository.save(harvest);

        // Keep the crop cycle's actual harvest date in sync.
        updateCropCycleHarvestDate(cycle);

        return mapToResponse(updated);
    }

    // Delete harvest record
    @Transactional
    public void deleteHarvest(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long harvestId,
            Long ownerId) {

        CropCycle cycle = getOwnedCycle(
                farmId, plotId, cycleId, ownerId);

        HarvestRecord harvest =
                getExistingHarvest(harvestId, cycleId);

        harvestRepository.delete(harvest);

        updateCropCycleHarvestDate(cycle);
    }

    private CropCycle getOwnedCycle(
            Long farmId,
            Long plotId,
            Long cycleId,
            Long ownerId) {

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

    private HarvestRecord getExistingHarvest(
            Long harvestId, Long cycleId) {

        return harvestRepository
                .findByIdAndCropCycleId(harvestId, cycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Harvest record not found"));
    }

    private void validateRequest(HarvestRequest request) {

        String unit = request.getQuantityUnit()
                .trim().toUpperCase();

        if (!VALID_UNITS.contains(unit)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid quantity unit. Allowed: KG, TONNE, QUINTAL, GRAM");
        }

        if (request.getDamagedQuantity() != null
                && request.getDamagedQuantity()
                .compareTo(request.getQuantity()) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Damaged quantity cannot exceed total harvest quantity");
        }

        if (request.getQualityGrade() != null
                && !request.getQualityGrade().isBlank()
                && !VALID_QUALITY_GRADES.contains(
                        request.getQualityGrade().trim().toUpperCase())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid quality grade. Allowed: A, B, C, PREMIUM, STANDARD, LOW");
        }
    }

    private void updateFields(
            HarvestRecord harvest,
            HarvestRequest request) {

        harvest.setHarvestDate(request.getHarvestDate());
        harvest.setQuantity(request.getQuantity());
        harvest.setQuantityUnit(
                request.getQuantityUnit().trim().toUpperCase());

        harvest.setQualityGrade(
                request.getQualityGrade() == null
                        ? null
                        : request.getQualityGrade().trim().toUpperCase());

        harvest.setDamagedQuantity(
                request.getDamagedQuantity() == null
                        ? BigDecimal.ZERO
                        : request.getDamagedQuantity());

        harvest.setNotes(request.getNotes());
    }

    private void updateCropCycleHarvestDate(CropCycle cycle) {

        List<HarvestRecord> records =
                harvestRepository.findByCropCycleIdOrderByHarvestDateDesc(
                        cycle.getId());

        if (records.isEmpty()) {
            cycle.setActualHarvestDate(null);
            cycle.setStatus("ACTIVE");
        } else {
            cycle.setActualHarvestDate(records.get(0).getHarvestDate());
            cycle.setStatus("COMPLETED");
        }

        cropCycleRepository.save(cycle);
    }

    private HarvestResponse mapToResponse(HarvestRecord harvest) {

        return new HarvestResponse(
                harvest.getId(),
                harvest.getCropCycle().getId(),
                harvest.getCropCycle().getCropName(),
                harvest.getHarvestDate(),
                harvest.getQuantity(),
                harvest.getQuantityUnit(),
                harvest.getQualityGrade(),
                harvest.getDamagedQuantity(),
                harvest.getNotes(),
                harvest.getCreatedAt(),
                harvest.getUpdatedAt()
        );
    }
}