package com.backend.service;

import com.backend.dto.inventory.SupplierRequest;
import com.backend.dto.inventory.SupplierResponse;
import com.backend.entity.Farm;
import com.backend.entity.Supplier;
import com.backend.repository.FarmRepository;
import com.backend.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final FarmRepository farmRepository;

    public SupplierResponse createSupplier(Long farmId, Long userId, SupplierRequest request) {
        Farm farm = getOwnedFarm(farmId, userId);

        if (supplierRepository.existsByNameIgnoreCaseAndFarmId(request.getName(), farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A supplier with this name already exists"
            );
        }

        Supplier supplier = Supplier.builder()
                .farm(farm)
                .name(request.getName().trim())
                .contactPerson(request.getContactPerson())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .build();

        return toResponse(supplierRepository.save(supplier));
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getSuppliers(Long farmId, Long userId) {
        getOwnedFarm(farmId, userId);

        return supplierRepository.findByFarmId(farmId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplier(Long farmId, Long supplierId, Long userId) {
        getOwnedFarm(farmId, userId);

        return toResponse(findSupplier(farmId, supplierId));
    }

    public SupplierResponse updateSupplier(
            Long farmId, Long supplierId, Long userId, SupplierRequest request) {

        getOwnedFarm(farmId, userId);

        Supplier supplier = findSupplier(farmId, supplierId);
        String newName = request.getName().trim();

        if (!supplier.getName().equalsIgnoreCase(newName)
                && supplierRepository.existsByNameIgnoreCaseAndFarmId(newName, farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "A supplier with this name already exists"
            );
        }

        supplier.setName(newName);
        supplier.setContactPerson(request.getContactPerson());
        supplier.setPhone(request.getPhone());
        supplier.setEmail(request.getEmail());
        supplier.setAddress(request.getAddress());

        return toResponse(supplierRepository.save(supplier));
    }

    public void deleteSupplier(Long farmId, Long supplierId, Long userId) {
        getOwnedFarm(farmId, userId);

        supplierRepository.delete(findSupplier(farmId, supplierId));
    }

    private Farm getOwnedFarm(Long farmId, Long userId) {
        return farmRepository.findByIdAndOwnerId(farmId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"
                ));
    }

    private Supplier findSupplier(Long farmId, Long supplierId) {
        return supplierRepository.findByIdAndFarmId(supplierId, farmId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Supplier not found"
                ));
    }

    private SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .farmId(supplier.getFarm().getId())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .phone(supplier.getPhone())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .createdAt(supplier.getCreatedAt())
                .updatedAt(supplier.getUpdatedAt())
                .build();
    }
}