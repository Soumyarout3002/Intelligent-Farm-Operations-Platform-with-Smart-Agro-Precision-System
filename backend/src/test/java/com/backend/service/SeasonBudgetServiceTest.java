
package com.backend.service;

import com.backend.dto.budget.BudgetCategoryResponse;
import com.backend.dto.budget.SeasonBudgetResponse;
import com.backend.entity.BudgetAllocation;
import com.backend.entity.CropCycle;
import com.backend.entity.Farm;
import com.backend.entity.Plot;
import com.backend.entity.SeasonBudget;
import com.backend.repository.BudgetAllocationRepository;
import com.backend.repository.CropActivityRepository;
import com.backend.repository.CropCycleRepository;
import com.backend.repository.EquipmentUsageRepository;
import com.backend.repository.FarmRepository;
import com.backend.repository.LabourAttendanceRepository;
import com.backend.repository.SeasonBudgetRepository;
import com.backend.repository.StockMovementRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeasonBudgetServiceTest {

    private SeasonBudgetRepository seasonBudgetRepository;
    private BudgetAllocationRepository budgetAllocationRepository;
    private CropCycleRepository cropCycleRepository;
    private FarmRepository farmRepository;
    private LabourAttendanceRepository labourAttendanceRepository;
    private EquipmentUsageRepository equipmentUsageRepository;
    private CropActivityRepository cropActivityRepository;
    private StockMovementRepository stockMovementRepository;

    private SeasonBudgetService seasonBudgetService;

    private Farm farm;
    private Plot plot;
    private CropCycle cropCycle;
    private SeasonBudget budget;
    private BudgetAllocation allocation;

    @BeforeEach
    void setUp() {

        seasonBudgetRepository = mock(SeasonBudgetRepository.class);
        budgetAllocationRepository = mock(BudgetAllocationRepository.class);
        cropCycleRepository = mock(CropCycleRepository.class);
        farmRepository = mock(FarmRepository.class);
        labourAttendanceRepository = mock(LabourAttendanceRepository.class);
        equipmentUsageRepository = mock(EquipmentUsageRepository.class);
        cropActivityRepository = mock(CropActivityRepository.class);
        stockMovementRepository = mock(StockMovementRepository.class);

        seasonBudgetService = new SeasonBudgetService(
                seasonBudgetRepository,
                budgetAllocationRepository,
                cropCycleRepository,
                farmRepository,
                labourAttendanceRepository,
                equipmentUsageRepository,
                cropActivityRepository,
                stockMovementRepository
        );

        farm = mock(Farm.class);
        plot = mock(Plot.class);
        cropCycle = mock(CropCycle.class);
        budget = mock(SeasonBudget.class);
        allocation = mock(BudgetAllocation.class);

        when(farm.getId()).thenReturn(1L);
        when(plot.getFarm()).thenReturn(farm);
        when(cropCycle.getPlot()).thenReturn(plot);
        when(cropCycle.getId()).thenReturn(1L);

        when(budget.getId()).thenReturn(1L);
        when(budget.getCropCycle()).thenReturn(cropCycle);
        when(budget.getTotalBudget()).thenReturn(new BigDecimal("10000"));

        when(farmRepository.findByIdAndOwnerId(1L, 1L))
                .thenReturn(Optional.of(farm));

        when(seasonBudgetRepository.findById(1L))
                .thenReturn(Optional.of(budget));

        when(allocation.getId()).thenReturn(1L);
        when(allocation.getCategory()).thenReturn("LABOUR");
        when(allocation.getAllocatedAmount())
                .thenReturn(new BigDecimal("10000"));
        when(allocation.getNotes()).thenReturn("Labour budget");

        when(budgetAllocationRepository.findBySeasonBudgetId(1L))
                .thenReturn(List.of(allocation));
    }

    @Test
    void budgetAlertShouldBeFalseBelow90Percent() {

        when(labourAttendanceRepository
                .sumDailyWageByCropCycleIdAndStatus(1L, "PRESENT"))
                .thenReturn(new BigDecimal("8999"));

        SeasonBudgetResponse response =
                seasonBudgetService.getBudget(1L, 1L, 1L);

        assertFalse(response.getAlert());

        BudgetCategoryResponse category =
                response.getAllocations().get(0);

        assertFalse(category.getAlert());
        assertEquals(
                new BigDecimal("89.99"),
                category.getUtilizationPercentage()
        );
    }

    @Test
    void budgetAlertShouldBeTrueAtExactly90Percent() {

        when(labourAttendanceRepository
                .sumDailyWageByCropCycleIdAndStatus(1L, "PRESENT"))
                .thenReturn(new BigDecimal("9000"));

        SeasonBudgetResponse response =
                seasonBudgetService.getBudget(1L, 1L, 1L);

        assertTrue(response.getAlert());

        BudgetCategoryResponse category =
                response.getAllocations().get(0);

        assertTrue(category.getAlert());
        assertEquals(
                new BigDecimal("90.00"),
                category.getUtilizationPercentage()
        );
    }

@Test
void categoryAlertShouldBeTrueAt95Percent() {

    when(labourAttendanceRepository
            .sumDailyWageByCropCycleIdAndStatus(1L, "PRESENT"))
            .thenReturn(new BigDecimal("9500"));

    SeasonBudgetResponse response =
            seasonBudgetService.getBudget(1L, 1L, 1L);

    assertTrue(response.getAllocations().get(0).getAlert());
    assertEquals(
            new BigDecimal("95.00"),
            response.getAllocations().get(0).getUtilizationPercentage()
    );
}


@Test
void categoryAlertCanBeTrueWhileOverallAlertIsFalse() {

    // Overall season budget = ₹20,000
    when(budget.getTotalBudget())
            .thenReturn(new BigDecimal("20000"));

    // Labour allocation = ₹10,000
    // Actual labour expense = ₹9,000
    when(labourAttendanceRepository
            .sumDailyWageByCropCycleIdAndStatus(1L, "PRESENT"))
            .thenReturn(new BigDecimal("9000"));

    SeasonBudgetResponse response =
            seasonBudgetService.getBudget(1L, 1L, 1L);

    // Category utilization = 90%
    assertTrue(response.getAllocations().get(0).getAlert());

    assertEquals(
            new BigDecimal("90.00"),
            response.getAllocations().get(0).getUtilizationPercentage()
    );

    // Overall utilization = 45%
    assertFalse(response.getAlert());
}

}
