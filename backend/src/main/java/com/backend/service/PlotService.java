
package com.backend.service;

import com.backend.dto.PlotRequest;
import com.backend.dto.PlotResponse;
import com.backend.entity.Farm;
import com.backend.entity.Plot;
import com.backend.repository.FarmRepository;
import com.backend.repository.PlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlotService {

    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;

    public PlotResponse createPlot(
            Long farmId, PlotRequest request, Long ownerId) {

        Farm farm = getOwnedFarm(farmId, ownerId);

        if (plotRepository.existsByFarmIdAndNameIgnoreCase(
                farmId, request.getName().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A plot with this name already exists in this farm"
            );
        }

        Plot plot = Plot.builder()
                .name(request.getName().trim())
                .plotCode(request.getPlotCode())
                .area(request.getArea())
                .areaUnit(request.getAreaUnit())
                .description(request.getDescription())
                .farm(farm)
                .build();

        return toResponse(plotRepository.save(plot));
    }

    @Transactional(readOnly = true)
    public List<PlotResponse> getFarmPlots(
            Long farmId, Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return plotRepository.findByFarmId(farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlotResponse getPlot(
            Long farmId, Long plotId, Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        return toResponse(getPlotById(farmId, plotId));
    }

    public PlotResponse updatePlot(
            Long farmId,
            Long plotId,
            PlotRequest request,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        Plot plot = getPlotById(farmId, plotId);

        if (plotRepository.existsByFarmIdAndNameIgnoreCaseAndIdNot(
                farmId, request.getName().trim(), plotId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Another plot already uses this name"
            );
        }

        plot.setName(request.getName().trim());
        plot.setPlotCode(request.getPlotCode());
        plot.setArea(request.getArea());
        plot.setAreaUnit(request.getAreaUnit());
        plot.setDescription(request.getDescription());

        return toResponse(plotRepository.save(plot));
    }

    public void deletePlot(
            Long farmId, Long plotId, Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        Plot plot = getPlotById(farmId, plotId);

        plotRepository.delete(plot);
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {
        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));
    }

    private Plot getPlotById(Long farmId, Long plotId) {
        return plotRepository.findByIdAndFarmId(plotId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plot not found"));
    }

    private PlotResponse toResponse(Plot plot) {
        return PlotResponse.builder()
                .id(plot.getId())
                .name(plot.getName())
                .plotCode(plot.getPlotCode())
                .area(plot.getArea())
                .areaUnit(plot.getAreaUnit())
                .status(plot.getStatus())
                .description(plot.getDescription())
                .farmId(plot.getFarm().getId())
                .farmName(plot.getFarm().getName())
                .createdAt(plot.getCreatedAt())
                .updatedAt(plot.getUpdatedAt())
                .build();
    }
}
