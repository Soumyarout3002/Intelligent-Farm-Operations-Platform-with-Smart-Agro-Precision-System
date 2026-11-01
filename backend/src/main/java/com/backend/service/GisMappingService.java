package com.backend.service;

import com.backend.dto.gis.PlotBoundaryRequest;
import com.backend.dto.gis.PlotBoundaryResponse;
import com.backend.entity.Farm;
import com.backend.entity.Plot;
import com.backend.repository.FarmRepository;
import com.backend.repository.PlotRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GisMappingService {

    private static final double EARTH_RADIUS_METERS = 6_378_137.0;

    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;

    public PlotBoundaryResponse saveBoundary(
            Long farmId,
            Long plotId,
            Long ownerId,
            PlotBoundaryRequest request) {

        Plot plot = getOwnedPlot(farmId, plotId, ownerId);

        List<List<Double>> coordinates =
                validateAndCloseCoordinates(request.getCoordinates());

        double areaInSquareMeters = calculateArea(coordinates);

        if (areaInSquareMeters <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The boundary must have a valid area");
        }

        String geoJson = buildGeoJson(coordinates);

        BigDecimal calculatedArea = convertArea(
                areaInSquareMeters, plot.getAreaUnit());

        plot.setBoundaryGeoJson(geoJson);
        plot.setArea(calculatedArea);

        return mapToResponse(plotRepository.save(plot));
    }

    @Transactional(readOnly = true)
    public PlotBoundaryResponse getBoundary(
            Long farmId,
            Long plotId,
            Long ownerId) {

        Plot plot = getOwnedPlot(farmId, plotId, ownerId);

        if (plot.getBoundaryGeoJson() == null
                || plot.getBoundaryGeoJson().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No boundary has been saved for this plot");
        }

        return mapToResponse(plot);
    }

    public PlotBoundaryResponse updateBoundary(
            Long farmId,
            Long plotId,
            Long ownerId,
            PlotBoundaryRequest request) {

        // Saving a new polygon replaces the existing boundary.
        return saveBoundary(farmId, plotId, ownerId, request);
    }

    public void deleteBoundary(
            Long farmId,
            Long plotId,
            Long ownerId) {

        Plot plot = getOwnedPlot(farmId, plotId, ownerId);

        if (plot.getBoundaryGeoJson() == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No boundary exists for this plot");
        }

        plot.setBoundaryGeoJson(null);

        plotRepository.save(plot);

        // The previous area is retained because it may have been
        // entered manually before GIS boundary mapping.
    }

    private Plot getOwnedPlot(
            Long farmId,
            Long plotId,
            Long ownerId) {

        Farm farm = farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));

        return plotRepository.findByIdAndFarmId(plotId, farm.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Plot not found"));
    }

    private List<List<Double>> validateAndCloseCoordinates(
            List<List<Double>> coordinates) {

        if (coordinates == null || coordinates.size() < 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A polygon needs at least three coordinates");
        }

        List<List<Double>> validated = new ArrayList<>();

        for (List<Double> point : coordinates) {

            if (point == null || point.size() != 2
                    || point.get(0) == null
                    || point.get(1) == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Each coordinate must contain longitude and latitude");
            }

            double longitude = point.get(0);
            double latitude = point.get(1);

            if (!Double.isFinite(longitude)
                    || !Double.isFinite(latitude)
                    || longitude < -180 || longitude > 180
                    || latitude < -90 || latitude > 90) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid longitude or latitude");
            }

            validated.add(List.of(longitude, latitude));
        }

        long distinctPoints = validated.stream()
                .distinct()
                .count();

        if (distinctPoints < 3) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A polygon needs at least three distinct points");
        }

        List<Double> first = validated.get(0);
        List<Double> last = validated.get(validated.size() - 1);

        if (!first.equals(last)) {
            validated.add(new ArrayList<>(first));
        }

        return validated;
    }

    // Calculates the approximate area of a polygon on a spherical Earth.
    
    private double calculateArea(List<List<Double>> coordinates) {

        double sum = 0;

        for (int i = 0; i < coordinates.size() - 1; i++) {

            double lon1 = Math.toRadians(coordinates.get(i).get(0));
            double lat1 = Math.toRadians(coordinates.get(i).get(1));

            double lon2 = Math.toRadians(coordinates.get(i + 1).get(0));
            double lat2 = Math.toRadians(coordinates.get(i + 1).get(1));

            sum += (lon2 - lon1)
                    * (2 + Math.sin(lat1) + Math.sin(lat2));
        }

        return Math.abs(sum * EARTH_RADIUS_METERS
                * EARTH_RADIUS_METERS / 2.0);
    }

    private BigDecimal convertArea(
            double squareMeters, String areaUnit) {

        double convertedArea;

        switch (areaUnit.toUpperCase()) {
            case "ACRE" ->
                    convertedArea = squareMeters / 4046.8564224;

            case "HECTARE" ->
                    convertedArea = squareMeters / 10000.0;

            case "SQ_METER" ->
                    convertedArea = squareMeters;

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported plot area unit");
        }

        return BigDecimal.valueOf(convertedArea)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String buildGeoJson(List<List<Double>> coordinates) {

        StringBuilder json = new StringBuilder();

        json.append("{\"type\":\"Polygon\",\"coordinates\":[[");

        for (int i = 0; i < coordinates.size(); i++) {

            List<Double> point = coordinates.get(i);

            if (i > 0) {
                json.append(",");
            }

            json.append("[")
                    .append(point.get(0))
                    .append(",")
                    .append(point.get(1))
                    .append("]");
        }

        json.append("]]}");

        return json.toString();
    }

    private PlotBoundaryResponse mapToResponse(Plot plot) {

        return PlotBoundaryResponse.builder()
                .plotId(plot.getId())
                .plotName(plot.getName())
                .boundaryGeoJson(plot.getBoundaryGeoJson())
                .area(plot.getArea())
                .areaUnit(plot.getAreaUnit())
                .build();
    }
}
