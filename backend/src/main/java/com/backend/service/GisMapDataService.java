package com.backend.service;

import com.backend.dto.gis.FarmMapResponse;
import com.backend.dto.gis.PlotMapResponse;
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
@Transactional(readOnly = true)
public class GisMapDataService {

    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;

    public List<FarmMapResponse> getMyFarmsMapData(Long ownerId) {

        List<Farm> farms = farmRepository.findByOwnerId(ownerId);

        return farms.stream()
                .map(this::mapFarmToResponse)
                .toList();
    }

    public FarmMapResponse getFarmMapData(Long farmId, Long ownerId) {

        Farm farm = farmRepository
                .findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        return mapFarmToResponse(farm);
    }

    private FarmMapResponse mapFarmToResponse(Farm farm) {

        List<Plot> plots = plotRepository.findByFarmId(farm.getId());

        List<PlotMapResponse> plotResponses = plots.stream()
                .map(plot -> mapPlotToResponse(plot, farm))
                .toList();

        return FarmMapResponse.builder()
                .farmId(farm.getId())
                .farmName(farm.getName())
                .location(farm.getLocation())
                .farmArea(farm.getArea())
                .farmAreaUnit(farm.getAreaUnit())
                .primaryCrop(farm.getPrimaryCrop())
                .farmStatus(farm.getStatus())
                .plots(plotResponses)
                .build();
    }

    public List<FarmMapResponse> filterMyFarms(
        Long ownerId,
        String farmName,
        String farmStatus,
        String crop,
        String plotName,
        String plotStatus) {

    List<Farm> farms = farmRepository.findByOwnerId(ownerId);

    return farms.stream()
            .filter(farm -> farmName == null
                    || farm.getName().toLowerCase()
                    .contains(farmName.toLowerCase()))
            .filter(farm -> farmStatus == null
                    || farm.getStatus().equalsIgnoreCase(farmStatus))
            .filter(farm -> crop == null
                    || (farm.getPrimaryCrop() != null
                    && farm.getPrimaryCrop().toLowerCase()
                    .contains(crop.toLowerCase())))
            .map(farm -> mapFarmToFilteredResponse(
                    farm, plotName, plotStatus))
            .toList();
}

public List<PlotMapResponse> filterPlots(Long farmId,Long ownerId,String plotName,String plotStatus,String crop) {

    Farm farm = farmRepository.findByIdAndOwnerId(farmId, ownerId)
            .orElseThrow(() ->
                    new RuntimeException("Farm not found"));

    return plotRepository.findByFarmId(farmId)
            .stream()
            .filter(plot -> plotName == null
                    || plot.getName().toLowerCase()
                    .contains(plotName.toLowerCase()))
            .filter(plot -> plotStatus == null
                    || plot.getStatus().equalsIgnoreCase(plotStatus))
            .filter(plot -> crop == null
                    || (farm.getPrimaryCrop() != null
                    && farm.getPrimaryCrop().toLowerCase()
                    .contains(crop.toLowerCase())))
            .map(plot -> new PlotMapResponse(
                    plot.getId(),
                    plot.getName(),
                    plot.getPlotCode(),
                    plot.getArea(),
                    plot.getAreaUnit(),
                    plot.getStatus(),
                    plot.getBoundaryGeoJson(),
                    farm.getPrimaryCrop(),
                    plot.getDescription()
            ))
            .toList();
}

    private PlotMapResponse mapPlotToResponse(Plot plot, Farm farm) {

        return PlotMapResponse.builder()
                .plotId(plot.getId())
                .plotName(plot.getName())
                .plotCode(plot.getPlotCode())
                .area(plot.getArea())
                .areaUnit(plot.getAreaUnit())
                .status(plot.getStatus())
                .boundaryGeoJson(plot.getBoundaryGeoJson())
                .cropName(farm.getPrimaryCrop())
                .description(plot.getDescription())
                .build();
    }

    private FarmMapResponse mapFarmToFilteredResponse(Farm farm,String plotName,String plotStatus) {

    List<PlotMapResponse> filteredPlots =
            plotRepository.findByFarmId(farm.getId())
                    .stream()
                    .filter(plot -> plotName == null
                            || plot.getName().toLowerCase()
                            .contains(plotName.toLowerCase()))
                    .filter(plot -> plotStatus == null
                            || plot.getStatus().equalsIgnoreCase(plotStatus))
                    .map(plot -> new PlotMapResponse(
                            plot.getId(),
                            plot.getName(),
                            plot.getPlotCode(),
                            plot.getArea(),
                            plot.getAreaUnit(),
                            plot.getStatus(),
                            plot.getBoundaryGeoJson(),
                            farm.getPrimaryCrop(),
                            plot.getDescription()
                    ))
                    .toList();

    return new FarmMapResponse(
            farm.getId(),
            farm.getName(),
            farm.getLocation(),
            farm.getArea(),
            farm.getAreaUnit(),
            farm.getPrimaryCrop(),
            farm.getStatus(),
            filteredPlots
    );
}
}
