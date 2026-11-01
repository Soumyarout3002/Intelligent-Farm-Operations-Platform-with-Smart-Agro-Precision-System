package com.backend.service;

import com.backend.dto.crop.*;
import com.backend.entity.*;
import com.backend.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class CropTrackingService {

    private final GrowthStageRecordRepository stageRepository;
    private final CropActivityRepository activityRepository;
    private final CropCycleRepository cropCycleRepository;
    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;

    private static final Set<String> VALID_STAGES = Set.of(
            "GERMINATION", "SEEDLING", "TILLERING",
            "FLOWERING", "FRUITING", "MATURITY", "HARVEST"
    );

    private static final Set<String> VALID_ACTIVITIES = Set.of(
            "IRRIGATION", "FERTILIZATION", "WEEDING",
            "PRUNING", "PEST_CONTROL", "SOIL_PREPARATION",
            "SPRAYING", "OTHER"
    );

    public CropTrackingService(
            GrowthStageRecordRepository stageRepository,
            CropActivityRepository activityRepository,
            CropCycleRepository cropCycleRepository,
            FarmRepository farmRepository,
            PlotRepository plotRepository) {

        this.stageRepository = stageRepository;
        this.activityRepository = activityRepository;
        this.cropCycleRepository = cropCycleRepository;
        this.farmRepository = farmRepository;
        this.plotRepository = plotRepository;
    }

    //GROWTH STAGES

    @Transactional
    public GrowthStageResponse addGrowthStage(
            Long farmId, Long plotId, Long cycleId,
            Long ownerId, GrowthStageRequest request) {

        CropCycle cycle = getOwnedCycle(farmId, plotId, cycleId, ownerId);

        validateStage(request);

        GrowthStageRecord stage = new GrowthStageRecord();
        stage.setCropCycle(cycle);
        stage.setStageName(request.getStageName().trim().toUpperCase());
        stage.setStartDate(request.getStartDate());
        stage.setEndDate(request.getEndDate());
        stage.setObservations(request.getObservations());

        return mapStage(stageRepository.save(stage));
    }

    @Transactional(readOnly = true)
    public List<GrowthStageResponse> getGrowthStages(
            Long farmId, Long plotId, Long cycleId, Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        return stageRepository
                .findByCropCycleIdOrderByStartDateAsc(cycleId)
                .stream()
                .map(this::mapStage)
                .toList();
    }

    @Transactional
    public GrowthStageResponse updateGrowthStage(
            Long farmId, Long plotId, Long cycleId,
            Long stageId, Long ownerId, GrowthStageRequest request) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);
        validateStage(request);

        GrowthStageRecord stage = stageRepository
                .findByIdAndCropCycleId(stageId, cycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Growth stage not found"));

        stage.setStageName(request.getStageName().trim().toUpperCase());
        stage.setStartDate(request.getStartDate());
        stage.setEndDate(request.getEndDate());
        stage.setObservations(request.getObservations());

        return mapStage(stageRepository.save(stage));
    }

    @Transactional
    public void deleteGrowthStage(
            Long farmId, Long plotId, Long cycleId,
            Long stageId, Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        GrowthStageRecord stage = stageRepository
                .findByIdAndCropCycleId(stageId, cycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Growth stage not found"));

        stageRepository.delete(stage);
    }

    //FARMING ACTIVITIES

    @Transactional
    public CropActivityResponse addActivity(
            Long farmId, Long plotId, Long cycleId,
            Long ownerId, CropActivityRequest request) {

        CropCycle cycle = getOwnedCycle(farmId, plotId, cycleId, ownerId);

        validateActivity(request);

        CropActivity activity = new CropActivity();
        activity.setCropCycle(cycle);
        activity.setActivityType(
        request.getActivityType().trim().toUpperCase());
        activity.setTitle(request.getTitle().trim());
        activity.setActivityDate(request.getActivityDate());
        activity.setDescription(request.getDescription());
        activity.setQuantity(request.getQuantity());
        activity.setPerformedBy(request.getPerformedBy());

        return mapActivity(activityRepository.save(activity));
    }

    @Transactional(readOnly = true)
    public List<CropActivityResponse> getActivities(
            Long farmId, Long plotId, Long cycleId, Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        return activityRepository
                .findByCropCycleIdOrderByActivityDateDesc(cycleId)
                .stream()
                .map(this::mapActivity)
                .toList();
    }

    @Transactional
    public CropActivityResponse updateActivity(
            Long farmId, Long plotId, Long cycleId,
            Long activityId, Long ownerId, CropActivityRequest request) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);
        validateActivity(request);

        CropActivity activity = activityRepository
                .findByIdAndCropCycleId(activityId, cycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Activity not found"));

       activity.setActivityType(
        request.getActivityType().trim().toUpperCase());
        activity.setTitle(request.getTitle().trim());
        activity.setActivityDate(request.getActivityDate());
        activity.setDescription(request.getDescription());
        activity.setQuantity(request.getQuantity());
        activity.setPerformedBy(request.getPerformedBy());

        return mapActivity(activityRepository.save(activity));
    }

    @Transactional
    public void deleteActivity(
            Long farmId, Long plotId, Long cycleId,
            Long activityId, Long ownerId) {

        getOwnedCycle(farmId, plotId, cycleId, ownerId);

        CropActivity activity = activityRepository
                .findByIdAndCropCycleId(activityId, cycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Activity not found"));

        activityRepository.delete(activity);
    }

    //VALIDATION

private void validateStage(GrowthStageRequest request) {

    String stage = request.getStageName().trim().toUpperCase();

    if (!VALID_STAGES.contains(stage)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid stage. Allowed: " + VALID_STAGES);
    }

    if (request.getEndDate() != null
            && request.getEndDate().isBefore(request.getStartDate())) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Stage end date cannot be before start date");
    }
}

    private void validateActivity(CropActivityRequest request) {

    String type = request.getActivityType().trim().toUpperCase();

    if (!VALID_ACTIVITIES.contains(type)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid activity type. Allowed: " + VALID_ACTIVITIES);
    }
}
    //OWNERSHIP

    private CropCycle getOwnedCycle(
            Long farmId, Long plotId, Long cycleId, Long ownerId) {

        Farm farm = farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        plotRepository.findByIdAndFarmId(plotId, farm.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plot not found"));

        return cropCycleRepository.findByIdAndPlotId(cycleId, plotId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crop cycle not found"));
    }

    //RESPONSE MAPPING

    private GrowthStageResponse mapStage(GrowthStageRecord stage) {

        return new GrowthStageResponse(
                stage.getId(),
                stage.getCropCycle().getId(),
                stage.getStageName(),
                stage.getStartDate(),
                stage.getEndDate(),
                stage.getObservations(),
                stage.getCreatedAt()
        );
    }

    private CropActivityResponse mapActivity(CropActivity activity) {

        return new CropActivityResponse(
                activity.getId(),
                activity.getCropCycle().getId(),
                activity.getActivityType(),
                activity.getTitle(),
                activity.getActivityDate(),
                activity.getDescription(),
                activity.getQuantity(),
                activity.getPerformedBy(),
                activity.getCreatedAt()
        );
    }
}
