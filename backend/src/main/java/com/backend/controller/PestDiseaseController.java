package com.backend.controller;

import com.backend.dto.crop.PestDiseaseRequest;
import com.backend.dto.crop.PestDiseaseResponse;
import com.backend.entity.User;
import com.backend.service.PestDiseaseService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/farms/{farmId}/plots/{plotId}/crop-cycles/{cycleId}/observations"
)
public class PestDiseaseController {

    private final PestDiseaseService pestDiseaseService;

    public PestDiseaseController(PestDiseaseService pestDiseaseService) {
        this.pestDiseaseService = pestDiseaseService;
    }

    @PostMapping
    public ResponseEntity<PestDiseaseResponse> createObservation(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @Valid @RequestBody PestDiseaseRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pestDiseaseService.createObservation(
                        farmId, plotId, cycleId, user.getId(), request));
    }

    @GetMapping
    public ResponseEntity<List<PestDiseaseResponse>> getObservations(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                pestDiseaseService.getObservations(
                        farmId, plotId, cycleId, user.getId()));
    }

    @GetMapping("/{observationId}")
    public ResponseEntity<PestDiseaseResponse> getObservation(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long observationId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                pestDiseaseService.getObservation(
                        farmId, plotId, cycleId,
                        observationId, user.getId()));
    }

    @PutMapping("/{observationId}")
    public ResponseEntity<PestDiseaseResponse> updateObservation(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long observationId,
            @Valid @RequestBody PestDiseaseRequest request,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        return ResponseEntity.ok(
                pestDiseaseService.updateObservation(
                        farmId, plotId, cycleId,
                        observationId, user.getId(), request));
    }

    @DeleteMapping("/{observationId}")
    public ResponseEntity<Void> deleteObservation(
            @PathVariable Long farmId,
            @PathVariable Long plotId,
            @PathVariable Long cycleId,
            @PathVariable Long observationId,
            HttpServletRequest httpRequest) {

        User user = (User) httpRequest.getAttribute("authenticatedUser");

        pestDiseaseService.deleteObservation(
                farmId, plotId, cycleId,
                observationId, user.getId());

        return ResponseEntity.noContent().build();
    }
}
