package com.backend.service;

import com.backend.dto.SoilRecordRequest;
import com.backend.dto.SoilRecordResponse;
import com.backend.entity.Farm;
import com.backend.entity.Plot;
import com.backend.entity.SoilRecord;
import com.backend.repository.FarmRepository;
import com.backend.repository.PlotRepository;
import com.backend.repository.SoilRecordRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SoilRecordService {

    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;
    private final SoilRecordRepository soilRecordRepository;

    public SoilRecordResponse createRecord(
            Long farmId,
            Long plotId,
            Long ownerId,
            SoilRecordRequest request) {

        Plot plot = getOwnedPlot(farmId, plotId, ownerId);

        SoilRecord record = SoilRecord.builder()
                .plot(plot)
                .phLevel(request.getPhLevel())
                .nitrogen(request.getNitrogen())
                .phosphorus(request.getPhosphorus())
                .potassium(request.getPotassium())
                .organicMatter(request.getOrganicMatter())
                .moisture(request.getMoisture())
                .recordedAt(request.getRecordedAt())
                .notes(request.getNotes())
                .build();

        return mapToResponse(soilRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<SoilRecordResponse> getRecords(
            Long farmId,
            Long plotId,
            Long ownerId) {

        getOwnedPlot(farmId, plotId, ownerId);

        return soilRecordRepository
                .findByPlotIdOrderByRecordedAtDesc(plotId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SoilRecordResponse getRecord(
            Long farmId,
            Long plotId,
            Long recordId,
            Long ownerId) {

        getOwnedPlot(farmId, plotId, ownerId);

        SoilRecord record = soilRecordRepository
                .findByIdAndPlotId(recordId, plotId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Soil record not found"));

        return mapToResponse(record);
    }

    public SoilRecordResponse updateRecord(
            Long farmId,
            Long plotId,
            Long recordId,
            Long ownerId,
            SoilRecordRequest request) {

        getOwnedPlot(farmId, plotId, ownerId);

        SoilRecord record = soilRecordRepository
                .findByIdAndPlotId(recordId, plotId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Soil record not found"));

        record.setPhLevel(request.getPhLevel());
        record.setNitrogen(request.getNitrogen());
        record.setPhosphorus(request.getPhosphorus());
        record.setPotassium(request.getPotassium());
        record.setOrganicMatter(request.getOrganicMatter());
        record.setMoisture(request.getMoisture());
        record.setRecordedAt(request.getRecordedAt());
        record.setNotes(request.getNotes());

        return mapToResponse(soilRecordRepository.save(record));
    }

    public void deleteRecord(
            Long farmId,
            Long plotId,
            Long recordId,
            Long ownerId) {

        getOwnedPlot(farmId, plotId, ownerId);

        SoilRecord record = soilRecordRepository
                .findByIdAndPlotId(recordId, plotId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Soil record not found"));

        soilRecordRepository.delete(record);
    }

    private Plot getOwnedPlot(Long farmId, Long plotId, Long ownerId) {

        Farm farm = farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        return plotRepository.findByIdAndFarmId(plotId, farm.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plot not found"));
    }

    private SoilRecordResponse mapToResponse(SoilRecord record) {

        return SoilRecordResponse.builder()
                .id(record.getId())
                .plotId(record.getPlot().getId())
                .plotName(record.getPlot().getName())
                .phLevel(record.getPhLevel())
                .nitrogen(record.getNitrogen())
                .phosphorus(record.getPhosphorus())
                .potassium(record.getPotassium())
                .organicMatter(record.getOrganicMatter())
                .moisture(record.getMoisture())
                .recordedAt(record.getRecordedAt())
                .createdAt(record.getCreatedAt())
                .notes(record.getNotes())
                .build();
    }
}
