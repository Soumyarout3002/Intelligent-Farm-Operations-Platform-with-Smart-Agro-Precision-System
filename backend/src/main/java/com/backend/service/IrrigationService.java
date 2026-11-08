package com.backend.service;

import com.backend.dto.irrigation.*;
import com.backend.dto.weather.DailyForecastResponse;
import com.backend.entity.Farm;
import com.backend.entity.IrrigationRecord;
import com.backend.entity.IrrigationSchedule;
import com.backend.repository.FarmRepository;
import com.backend.repository.IrrigationRecordRepository;
import com.backend.repository.IrrigationScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IrrigationService {

    private final FarmRepository farmRepository;
    private final IrrigationScheduleRepository scheduleRepository;
    private final IrrigationRecordRepository recordRepository;
    private final WeatherService weatherService;

    // 1. Create schedule

    @Transactional
    public IrrigationScheduleResponse createSchedule(
            Long farmId,
            Long ownerId,
            IrrigationScheduleRequest request) {

        Farm farm = getFarm(farmId, ownerId);

        IrrigationSchedule schedule = IrrigationSchedule.builder()
                .farm(farm)
                .scheduledAt(request.getScheduledAt())
                .waterAmountLiters(request.getWaterAmountLiters())
                .notes(request.getNotes())
                .status("SCHEDULED")
                .createdAt(LocalDateTime.now())
                .build();

        return toScheduleResponse(
                scheduleRepository.save(schedule));
    }

    // 2. Get all schedules
    @Transactional(readOnly = true)
    public List<IrrigationScheduleResponse> getSchedules(
            Long farmId, Long ownerId) {

        getFarm(farmId, ownerId);

        return scheduleRepository
        .findSchedulesWithFarm(farmId)
        .stream()
        .map(this::toScheduleResponse)
        .toList();      
    }

    // 3. Get one schedule
    @Transactional(readOnly=true)
    public IrrigationScheduleResponse getSchedule(
            Long farmId,
            Long scheduleId,
            Long ownerId) {

        getFarm(farmId, ownerId);

        return toScheduleResponse(
                getScheduleEntity(farmId, scheduleId));
    }

    // 4. Update schedule

    @Transactional
    public IrrigationScheduleResponse updateSchedule(
            Long farmId,
            Long scheduleId,
            Long ownerId,
            IrrigationScheduleRequest request) {

        getFarm(farmId, ownerId);

        IrrigationSchedule schedule =
                getScheduleEntity(farmId, scheduleId);

        if (!schedule.getStatus().equals("SCHEDULED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only scheduled irrigation can be updated"
            );
        }

        schedule.setScheduledAt(request.getScheduledAt());
        schedule.setWaterAmountLiters(
                request.getWaterAmountLiters());
        schedule.setNotes(request.getNotes());
        schedule.setUpdatedAt(LocalDateTime.now());

        return toScheduleResponse(
                scheduleRepository.save(schedule));
    }

    // 5. Delete schedule

    @Transactional
    public void deleteSchedule(
            Long farmId,
            Long scheduleId,
            Long ownerId) {

        getFarm(farmId, ownerId);

        IrrigationSchedule schedule =
                getScheduleEntity(farmId, scheduleId);

        if (!schedule.getStatus().equals("SCHEDULED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only scheduled irrigation can be deleted"
            );
        }

        scheduleRepository.delete(schedule);
    }

    // 6. Record completed or skipped irrigation

    @Transactional
    public IrrigationRecordResponse recordIrrigation(
            Long farmId,
            Long scheduleId,
            Long ownerId,
            IrrigationRecordRequest request) {

        getFarm(farmId, ownerId);

        IrrigationSchedule schedule =
                getScheduleEntity(farmId, scheduleId);

        if (!schedule.getStatus().equals("SCHEDULED")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This irrigation schedule has already been recorded"
            );
        }

        String action = request.getAction().toUpperCase();

        IrrigationRecord record = IrrigationRecord.builder()
                .schedule(schedule)
                .action(action)
                .notes(request.getNotes())
                .recordedAt(LocalDateTime.now())
                .build();

        schedule.setStatus(action);
        schedule.setUpdatedAt(LocalDateTime.now());

        scheduleRepository.save(schedule);

        return toRecordResponse(
                recordRepository.save(record));
    }

    // 7. Get irrigation history
    @Transactional(readOnly = true)
    public List<IrrigationRecordResponse> getRecords(
            Long farmId,
            Long scheduleId,
            Long ownerId) {

        getFarm(farmId, ownerId);

        getScheduleEntity(farmId, scheduleId);

        return recordRepository
                .findByScheduleIdOrderByRecordedAtDesc(scheduleId)
                .stream()
                .map(this::toRecordResponse)
                .toList();
    }

    // 8. Weather-based irrigation suggestions

    public List<IrrigationSuggestionResponse> getSuggestions(
            Long farmId, Long ownerId) {

        Farm farm = getFarm(farmId, ownerId);

        List<DailyForecastResponse> forecast =
                weatherService.getForecast(farmId, ownerId);

        List<IrrigationSuggestionResponse> suggestions =
                new ArrayList<>();

        for (DailyForecastResponse day : forecast) {

            Double rainfall = day.getPrecipitationMm();

            Integer rainProbability =
                    day.getPrecipitationProbabilityMax();

            Double maxTemperature =
                    day.getMaxTemperatureC();

            String recommendation;
            String reason;
            String priority;

            if (rainfall != null && rainfall >= 10.0) {

                recommendation = "POSTPONE_IRRIGATION";
                reason = "Rainfall of approximately "
                        + rainfall
                        + " mm is forecast. Review whether additional watering is needed.";
                priority = "HIGH";

            } else if (rainProbability != null
                    && rainProbability >= 70) {

                recommendation = "REVIEW_IRRIGATION";
                reason = "There is a "
                        + rainProbability
                        + "% chance of rain. Check the forecast before watering.";
                priority = "MEDIUM";

            } else if (maxTemperature != null
                    && maxTemperature >= 35.0) {

                recommendation = "CONSIDER_EARLY_IRRIGATION";
                reason = "High temperature is forecast. Check soil moisture and consider watering during cooler hours.";
                priority = "MEDIUM";

            } else {

                recommendation = "FOLLOW_SCHEDULE";
                reason = "No significant rainfall or high-temperature condition was detected in the forecast.";
                priority = "LOW";
            }

            suggestions.add(
                    IrrigationSuggestionResponse.builder()
                            .farmId(farm.getId())
                            .farmName(farm.getName())
                            .date(day.getDate())
                            .recommendation(recommendation)
                            .reason(reason)
                            .priority(priority)
                            .build()
            );
        }

        return suggestions;
    }

    // 9. Farm ownership check

    private Farm getFarm(Long farmId, Long ownerId) {

        return farmRepository
                .findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Farm not found"
                ));
    }

    // 10. Find schedule belonging to farm

    private IrrigationSchedule getScheduleEntity(
            Long farmId, Long scheduleId) {

       return scheduleRepository
        .findScheduleWithFarm(scheduleId, farmId)
        .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Irrigation schedule not found"
        ));
    }

    // 11. Map schedule response

    private IrrigationScheduleResponse toScheduleResponse(
            IrrigationSchedule schedule) {

        return IrrigationScheduleResponse.builder()
                .id(schedule.getId())
                .farmId(schedule.getFarm().getId())
                .farmName(schedule.getFarm().getName())
                .scheduledAt(schedule.getScheduledAt())
                .waterAmountLiters(schedule.getWaterAmountLiters())
                .notes(schedule.getNotes())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    // 12. Map record response

    private IrrigationRecordResponse toRecordResponse(
            IrrigationRecord record) {

        return IrrigationRecordResponse.builder()
                .id(record.getId())
                .scheduleId(record.getSchedule().getId())
                .action(record.getAction())
                .notes(record.getNotes())
                .recordedAt(record.getRecordedAt())
                .build();
    }
}