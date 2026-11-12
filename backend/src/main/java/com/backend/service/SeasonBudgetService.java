
package com.backend.service;

import com.backend.dto.budget.BudgetAllocationRequest;
import com.backend.dto.budget.BudgetCategoryResponse;
import com.backend.dto.budget.SeasonBudgetRequest;
import com.backend.dto.budget.SeasonBudgetResponse;
import com.backend.entity.BudgetAllocation;
import com.backend.entity.CropCycle;
import com.backend.entity.Farm;
import com.backend.entity.SeasonBudget;
import com.backend.repository.BudgetAllocationRepository;
import com.backend.repository.CropCycleRepository;
import com.backend.repository.FarmRepository;
import com.backend.repository.SeasonBudgetRepository;
import com.backend.repository.StockMovementRepository;
import com.backend.entity.StockMovement.MovementType;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.backend.repository.LabourAttendanceRepository;
import com.backend.repository.EquipmentUsageRepository;
import com.backend.repository.CropActivityRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class SeasonBudgetService {

    private final SeasonBudgetRepository seasonBudgetRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final CropCycleRepository cropCycleRepository;
    private final FarmRepository farmRepository;
    private final LabourAttendanceRepository labourAttendanceRepository;
    private final EquipmentUsageRepository equipmentUsageRepository;
    private final CropActivityRepository cropActivityRepository;
    private final StockMovementRepository stockMovementRepository;

    private static final Set<String> VALID_CATEGORIES = Set.of(
            "SEEDS",
            "FERTILIZERS",
            "PESTICIDES",
            "LABOUR",
            "MACHINERY",
            "CROP_ACTIVITY",
            "IRRIGATION",
            "OTHER"
    );

    public SeasonBudgetResponse createBudget(
            Long farmId,
            SeasonBudgetRequest request,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        CropCycle cropCycle = cropCycleRepository.findById(request.getCropCycleId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Crop cycle not found"));

        if (!cropCycle.getPlot().getFarm().getId().equals(farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Crop cycle not found in this farm");
        }

        if (seasonBudgetRepository.existsByCropCycleId(cropCycle.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Budget already exists for this crop cycle");
        }

        SeasonBudget budget = SeasonBudget.builder()
                .cropCycle(cropCycle)
                .totalBudget(request.getTotalBudget())
                .notes(request.getNotes())
                .build();

        return toResponse(seasonBudgetRepository.save(budget));
    }

    public BudgetCategoryResponse addAllocation(
            Long farmId,
            Long budgetId,
            BudgetAllocationRequest request,
            Long ownerId) {

        SeasonBudget budget = getOwnedBudget(farmId, budgetId, ownerId);

        String category = request.getCategory().trim().toUpperCase();

        if (!VALID_CATEGORIES.contains(category)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid budget category");
        }

        if (budgetAllocationRepository.existsBySeasonBudgetIdAndCategoryIgnoreCase(
                budgetId, category)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Category allocation already exists");
        }

        BigDecimal allocatedSoFar = budgetAllocationRepository
                .findBySeasonBudgetId(budgetId)
                .stream()
                .map(BudgetAllocation::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal newTotal = allocatedSoFar.add(request.getAllocatedAmount());

        if (newTotal.compareTo(budget.getTotalBudget()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total allocations cannot exceed the season budget");
        }

        BudgetAllocation allocation = BudgetAllocation.builder()
                .seasonBudget(budget)
                .category(category)
                .allocatedAmount(request.getAllocatedAmount())
                .notes(request.getNotes())
                .build();


        BudgetAllocation saved = budgetAllocationRepository.save(allocation);

        BigDecimal actualExpense =
                calculateActualExpense(budget, category);

        BigDecimal remainingAmount =
                saved.getAllocatedAmount().subtract(actualExpense);

        BigDecimal utilization =
                calculateUtilization(
                        actualExpense,
                        saved.getAllocatedAmount()
                );

        return BudgetCategoryResponse.builder()
                .id(saved.getId())
                .category(saved.getCategory())
                .allocatedAmount(saved.getAllocatedAmount())
                .actualExpense(actualExpense)
                .remainingAmount(remainingAmount)
                .utilizationPercentage(utilization)
                .alert(utilization.compareTo(new BigDecimal("90.00")) >= 0)
                .notes(saved.getNotes())
                .build();

    }

    @Transactional(readOnly = true)
    public SeasonBudgetResponse getBudget(
            Long farmId,
            Long budgetId,
            Long ownerId) {

        return toResponse(getOwnedBudget(farmId, budgetId, ownerId));
    }

    @Transactional(readOnly = true)
    public SeasonBudgetResponse getBudgetByCropCycle(
            Long farmId,
            Long cropCycleId,
            Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        SeasonBudget budget = seasonBudgetRepository.findByCropCycleId(cropCycleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Budget not found"));

        if (!budget.getCropCycle().getPlot().getFarm().getId().equals(farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Budget not found");
        }

        return toResponse(budget);
    }

    private Farm getOwnedFarm(Long farmId, Long ownerId) {
        return farmRepository.findByIdAndOwnerId(farmId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Farm not found"));
    }

    private SeasonBudget getOwnedBudget(
            Long farmId, Long budgetId, Long ownerId) {

        getOwnedFarm(farmId, ownerId);

        SeasonBudget budget = seasonBudgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Budget not found"));

        if (!budget.getCropCycle().getPlot().getFarm().getId().equals(farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Budget not found");
        }

        return budget;
    }


private SeasonBudgetResponse toResponse(SeasonBudget budget) {

    List<BudgetCategoryResponse> allocations =
            budgetAllocationRepository.findBySeasonBudgetId(budget.getId())
                    .stream()
                    .map(allocation -> {

                        BigDecimal actualExpense =
                                calculateActualExpense(
                                        budget,
                                        allocation.getCategory()
                                );

                        BigDecimal remainingAmount =
                                allocation.getAllocatedAmount()
                                        .subtract(actualExpense);

                        BigDecimal utilization =
                                calculateUtilization(
                                        actualExpense,
                                        allocation.getAllocatedAmount()
                                );

                        return BudgetCategoryResponse.builder()
                                .id(allocation.getId())
                                .category(allocation.getCategory())
                                .allocatedAmount(allocation.getAllocatedAmount())
                                .actualExpense(actualExpense)
                                .remainingAmount(remainingAmount)
                                .utilizationPercentage(utilization)
                                .alert(utilization.compareTo(new BigDecimal("90.00")) >= 0)
                                .notes(allocation.getNotes())
                                .build();
                    })
                    .toList();

    BigDecimal totalAllocated = allocations.stream()
            .map(BudgetCategoryResponse::getAllocatedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalActualExpense = allocations.stream()
        .map(BudgetCategoryResponse::getActualExpense)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal overallUtilization = calculateUtilization(
            totalActualExpense,
            budget.getTotalBudget()
    );

    boolean overallAlert =
            overallUtilization.compareTo(new BigDecimal("90.00")) >= 0;

    BigDecimal unallocatedBudget = budget.getTotalBudget()
        .subtract(totalAllocated);

    BigDecimal remainingBudget = budget.getTotalBudget()
        .subtract(totalActualExpense);

    CropCycle cropCycle = budget.getCropCycle();

    return SeasonBudgetResponse.builder()
            .id(budget.getId())
            .cropCycleId(cropCycle.getId())
            .cropName(cropCycle.getCropName())
            .season(cropCycle.getSeason())
            .totalBudget(budget.getTotalBudget())
            .totalAllocated(totalAllocated)
            .actualExpense(totalActualExpense)
            .alert(overallAlert)
            .unallocatedBudget(unallocatedBudget)
            .remainingBudget(remainingBudget)
            .allocations(allocations)
            .notes(budget.getNotes())
            .createdAt(budget.getCreatedAt())
            .updatedAt(budget.getUpdatedAt())
            .build();
}

    private BigDecimal calculateActualExpense(
            SeasonBudget budget,
            String category) {

        if ("LABOUR".equalsIgnoreCase(category)) {
            return labourAttendanceRepository
                    .sumDailyWageByCropCycleIdAndStatus(
                            budget.getCropCycle().getId(),
                            "PRESENT"
                    );
        }
        if ("MACHINERY".equalsIgnoreCase(category)) {
            return equipmentUsageRepository
                    .sumTotalCostByCropCycleId(
                            budget.getCropCycle().getId()
                    );
        }

        if ("CROP_ACTIVITY".equalsIgnoreCase(category)) {
            return cropActivityRepository.sumActivityCostByCropCycleId(
                    budget.getCropCycle().getId()
            );
        }
        if ("SEEDS".equalsIgnoreCase(category)) {
        return stockMovementRepository.sumConsumedCostByCropCycleAndCategory(
                budget.getCropCycle().getId(),
                MovementType.OUT,
                "Seeds"
        );
        }

        if ("FERTILIZERS".equalsIgnoreCase(category)) {
        return stockMovementRepository.sumConsumedCostByCropCycleAndCategory(
                budget.getCropCycle().getId(),
                MovementType.OUT,
                "Fertilizers"
        );
        }
        if ("PESTICIDES".equalsIgnoreCase(category)) {
        return stockMovementRepository.sumConsumedCostByCropCycleAndCategory(
                budget.getCropCycle().getId(),
                MovementType.OUT,
                "Pesticides"
        );
        }

        // Other categories will be integrated separately.
        return BigDecimal.ZERO;
    }
    private BigDecimal calculateUtilization(
            BigDecimal actualExpense,
            BigDecimal allocatedAmount) {

        if (allocatedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return actualExpense
                .multiply(BigDecimal.valueOf(100))
                .divide(allocatedAmount, 2, RoundingMode.HALF_UP);
    }
}
