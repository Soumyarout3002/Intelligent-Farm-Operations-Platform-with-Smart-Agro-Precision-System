package com.backend.service;

import com.backend.dto.farm.FarmRequest;
import com.backend.dto.farm.FarmResponse;
import com.backend.entity.Farm;
import com.backend.entity.User;
import com.backend.repository.FarmRepository;
import com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmService {

    private final FarmRepository farmRepository;
    private final UserRepository userRepository;
    private final com.backend.repository.FarmWorkerAssignmentRepository assignmentRepository;
    private final com.backend.repository.PlotRepository plotRepository;
    private final com.backend.repository.FarmDocumentRepository documentRepository;

    public FarmResponse createFarm(FarmRequest request, Long ownerId) {
        User owner = getUser(ownerId);
        if ("FIELD_WORKER".equalsIgnoreCase(owner.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Field workers cannot create farms.");
        }

        if (farmRepository.existsByNameIgnoreCaseAndOwnerId(
                request.getName().trim(), ownerId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A farm with this name already exists"
            );
        }

        Farm farm = Farm.builder()
                .name(request.getName().trim())
                .location(request.getLocation().trim())
                .area(request.getArea())
                .areaUnit(request.getAreaUnit())
                .soilType(request.getSoilType())
                .primaryCrop(request.getPrimaryCrop())
                .notes(request.getNotes())
                .owner(owner)
                .build();

        return toResponse(farmRepository.save(farm));
    }

    @Transactional(readOnly = true)
    public List<FarmResponse> getMyFarms(Long ownerId, String role) {
        List<Farm> farms;
        if ("ADMIN".equalsIgnoreCase(role)) farms = farmRepository.findAll();
        else if ("FIELD_WORKER".equalsIgnoreCase(role)) {
            farms = assignmentRepository.findByWorkerIdAndStatusIgnoreCase(ownerId, "ACTIVE")
                    .stream().map(a -> a.getFarm()).distinct().toList();
        } else farms = farmRepository.findByOwnerId(ownerId);
        return farms.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FarmResponse getFarmById(Long farmId, Long userId, String role) {
        return toResponse(getAccessibleFarm(farmId, userId, role));
    }

    public FarmResponse updateFarm(Long farmId, FarmRequest request, Long ownerId, String role) {
        Farm farm = getManageableFarm(farmId, ownerId, role);

        farm.setName(request.getName().trim());
        farm.setLocation(request.getLocation().trim());
        farm.setArea(request.getArea());
        farm.setAreaUnit(request.getAreaUnit());
        farm.setSoilType(request.getSoilType());
        farm.setPrimaryCrop(request.getPrimaryCrop());
        farm.setNotes(request.getNotes());

        return toResponse(farmRepository.save(farm));
    }

    public void deleteFarm(Long farmId, Long ownerId, String role) {
        Farm farm = getManageableFarm(farmId, ownerId, role);
        if (plotRepository.existsByFarmId(farmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Remove the farm's plots and dependent crop/GIS records before deleting this farm.");
        }
        if (documentRepository.existsByFarmId(farmId) || assignmentRepository.existsByFarmId(farmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Remove this farm's documents and worker assignments before deleting it.");
        }
        farmRepository.delete(farm);
    }

    private Farm getAccessibleFarm(Long farmId, Long userId, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) return farmRepository.findById(farmId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found"));
        if ("FIELD_WORKER".equalsIgnoreCase(role)) {
            boolean assigned = assignmentRepository.findByWorkerIdAndStatusIgnoreCase(userId, "ACTIVE")
                    .stream().anyMatch(a -> a.getFarm().getId().equals(farmId));
            if (!assigned) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found");
            return farmRepository.findById(farmId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Farm not found"));
        }
        return getOwnedFarm(farmId, userId);
    }

    private Farm getManageableFarm(Long farmId, Long userId, String role) {
        if ("FIELD_WORKER".equalsIgnoreCase(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Field workers have read-only farm access.");
        return getAccessibleFarm(farmId, userId, role);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {
        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));
    }

    private FarmResponse toResponse(Farm farm) {
        return FarmResponse.builder()
                .id(farm.getId())
                .name(farm.getName())
                .location(farm.getLocation())
                .area(farm.getArea())
                .areaUnit(farm.getAreaUnit())
                .soilType(farm.getSoilType())
                .primaryCrop(farm.getPrimaryCrop())
                .status(farm.getStatus())
                .notes(farm.getNotes())
                .ownerId(farm.getOwner().getId())
                .ownerName(farm.getOwner().getName())
                .createdAt(farm.getCreatedAt())
                .updatedAt(farm.getUpdatedAt())
                .build();
    }
}