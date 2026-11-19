package com.backend.service;

import com.backend.dto.analytics.DashboardAnalyticsResponse;
import com.backend.entity.*;
import com.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import com.backend.dto.analytics.YieldAnalyticsResponse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import com.backend.dto.analytics.CostProfitAnalyticsResponse;
import java.util.HashSet;
import java.util.Set;
import com.backend.dto.analytics.MachineryAnalyticsResponse;
import com.backend.dto.analytics.WaterUsageAnalyticsResponse;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final FarmRepository farmRepository;
    private final PlotRepository plotRepository;
    private final CropCycleRepository cropCycleRepository;
    private final HarvestRecordRepository harvestRecordRepository;
    private final SeasonBudgetRepository seasonBudgetRepository;
    private final IrrigationScheduleRepository irrigationScheduleRepository;
    private final EquipmentUsageRepository equipmentUsageRepository;
    private final LabourAttendanceRepository labourAttendanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CropActivityRepository cropActivityRepository;

    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getDashboardAnalytics(
            Long farmId,
            String cropName,
            LocalDate startDate,
            LocalDate endDate) {

        validateFilters(farmId, startDate, endDate);
        String normalizedCrop = normalizeFilter(cropName);

        // 1. Farms & Plots
        List<Farm> farms = farmRepository.findAll().stream()
                .filter(f -> farmId == null || f.getId().equals(farmId))
                .toList();

        List<Plot> plots = plotRepository.findAll().stream()
                .filter(p -> farmId == null || p.getFarm().getId().equals(farmId))
                .toList();

        BigDecimal totalCultivatedArea = plots.stream()
                .map(p -> safeDecimal(p.getArea()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Crop Cycles
        List<CropCycle> cropCycles = cropCycleRepository.findAll().stream()
                .filter(c -> farmId == null || c.getPlot().getFarm().getId().equals(farmId))
                .filter(c -> matchesCrop(c.getCropName(), normalizedCrop))
                .filter(c -> isWithinDateRange(c.getSowingDate(), startDate, endDate))
                .toList();

        long activeCropCycles = cropCycles.stream()
                .filter(c -> !"COMPLETED".equalsIgnoreCase(c.getStatus())
                        && !"CANCELLED".equalsIgnoreCase(c.getStatus()))
                .count();

        // 3. Harvest & Yield per Acre
        BigDecimal totalHarvestQuantity = harvestRecordRepository.findAll().stream()
                .filter(h -> farmId == null || h.getCropCycle().getPlot().getFarm().getId().equals(farmId))
                .filter(h -> matchesCrop(h.getCropCycle().getCropName(), normalizedCrop))
                .filter(h -> isWithinDateRange(h.getHarvestDate(), startDate, endDate))
                .map(h -> safeDecimal(h.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageYieldPerAcre = totalCultivatedArea.compareTo(BigDecimal.ZERO) > 0
                ? totalHarvestQuantity.divide(totalCultivatedArea, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // 4. Planned Budget
        BigDecimal totalPlannedBudget = seasonBudgetRepository.findAll().stream()
                .filter(b -> farmId == null || b.getCropCycle().getPlot().getFarm().getId().equals(farmId))
                .filter(b -> matchesCrop(b.getCropCycle().getCropName(), normalizedCrop))
                .filter(b -> isWithinDateRange(b.getCropCycle().getSowingDate(), startDate, endDate))
                .map(b -> safeDecimal(b.getTotalBudget()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Equipment Usage & Cost
        List<EquipmentUsage> equipmentUsages = equipmentUsageRepository.findAll().stream()
                .filter(e -> farmId == null || e.getEquipment().getFarm().getId().equals(farmId))
                .filter(e -> normalizedCrop == null || (e.getCropCycle() != null
                        && matchesCrop(e.getCropCycle().getCropName(), normalizedCrop)))
                .filter(e -> isWithinDateRange(e.getUsageDate(), startDate, endDate))
                .toList();

        BigDecimal totalMachineryHours = equipmentUsages.stream()
                .map(e -> safeDecimal(e.getOperatingHours()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEquipmentCost = equipmentUsages.stream()
                .map(e -> safeDecimal(e.getUsageCost()).add(safeDecimal(e.getFuelCost())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Labour Cost
        BigDecimal totalLabourCost = labourAttendanceRepository.findAll().stream()
                .filter(l -> !"ABSENT".equalsIgnoreCase(l.getStatus()))
                .filter(l -> farmId == null || (l.getCropCycle() != null
                        && l.getCropCycle().getPlot().getFarm().getId().equals(farmId)))
                .filter(l -> normalizedCrop == null || (l.getCropCycle() != null
                        && matchesCrop(l.getCropCycle().getCropName(), normalizedCrop)))
                .filter(l -> isWithinDateRange(l.getAttendanceDate(), startDate, endDate))
                .map(l -> safeDecimal(l.getDailyWage()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Inventory Consumption Cost (Only Stock OUT)
        BigDecimal totalInventoryCost = stockMovementRepository.findAll().stream()
                .filter(s -> s.getType() == StockMovement.MovementType.OUT)
                .filter(s -> farmId == null || s.getFarm().getId().equals(farmId))
                .filter(s -> isWithinDateRange(s.getCreatedAt().toLocalDate(), startDate, endDate))
                .map(s -> safeDecimal(s.getTotalCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOperationalCost = totalEquipmentCost
                .add(totalLabourCost)
                .add(totalInventoryCost);

        // 8. Water Usage (Farm & Date based)
        BigDecimal totalWaterUsedLiters = irrigationScheduleRepository.findAll().stream()
                .filter(i -> farmId == null || i.getFarm().getId().equals(farmId))
                .filter(i -> isWithinDateRange(i.getScheduledAt().toLocalDate(), startDate, endDate))
                .map(i -> i.getWaterAmountLiters() == null
                        ? BigDecimal.ZERO
                        : BigDecimal.valueOf(i.getWaterAmountLiters()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return DashboardAnalyticsResponse.builder()
                .totalFarms(farms.size())
                .totalPlots(plots.size())
                .totalCultivatedArea(totalCultivatedArea.setScale(2, RoundingMode.HALF_UP))
                .activeCropCycles(activeCropCycles)
                .totalHarvestQuantity(totalHarvestQuantity.setScale(2, RoundingMode.HALF_UP))
                .averageYieldPerAcre(averageYieldPerAcre)
                .totalPlannedBudget(totalPlannedBudget.setScale(2, RoundingMode.HALF_UP))
                .totalOperationalCost(totalOperationalCost.setScale(2, RoundingMode.HALF_UP))
                .totalEquipmentCost(totalEquipmentCost.setScale(2, RoundingMode.HALF_UP))
                .totalLabourCost(totalLabourCost.setScale(2, RoundingMode.HALF_UP))
                .totalInventoryCost(totalInventoryCost.setScale(2, RoundingMode.HALF_UP))
                .totalRevenue(null)
                .netProfit(null)
                .revenueDataAvailable(false)
                .totalWaterUsedLiters(totalWaterUsedLiters)
                .totalMachineryHours(totalMachineryHours.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    @Transactional(readOnly = true)
    public List<YieldAnalyticsResponse> getYieldAnalytics(
            Long farmId,
            String cropName,
            String season,
            LocalDate startDate,
            LocalDate endDate) {

        validateFilters(farmId, startDate, endDate);
        String normalizedCrop = normalizeFilter(cropName);
        String normalizedSeason = normalizeFilter(season);

        List<CropCycle> filteredCycles = cropCycleRepository.findAll().stream()
                .filter(c -> farmId == null || c.getPlot().getFarm().getId().equals(farmId))
                .filter(c -> matchesCrop(c.getCropName(), normalizedCrop))
                .filter(c -> normalizedSeason == null || (c.getSeason() != null
                        && c.getSeason().trim().toLowerCase().contains(normalizedSeason)))
                .toList();

        List<HarvestRecord> filteredHarvests = harvestRecordRepository.findAll().stream()
                .filter(h -> farmId == null || h.getCropCycle().getPlot().getFarm().getId().equals(farmId))
                .filter(h -> matchesCrop(h.getCropCycle().getCropName(), normalizedCrop))
                .filter(h -> normalizedSeason == null || (h.getCropCycle().getSeason() != null
                        && h.getCropCycle().getSeason().trim().toLowerCase().contains(normalizedSeason)))
                .filter(h -> isWithinDateRange(h.getHarvestDate(), startDate, endDate))
                .toList();

        // Group crop cycles by (cropName + season)
        Map<String, List<CropCycle>> cyclesByGroup = new LinkedHashMap<>();
        for (CropCycle cycle : filteredCycles) {
            String cName = cycle.getCropName() != null ? cycle.getCropName().trim() : "Unknown";
            String sName = (cycle.getSeason() != null && !cycle.getSeason().isBlank())
                    ? cycle.getSeason().trim()
                    : "Unspecified";
            String key = cName.toLowerCase() + "||" + sName.toLowerCase();
            cyclesByGroup.computeIfAbsent(key, k -> new ArrayList<>()).add(cycle);
        }

        // Group harvests by cropCycleId for fast lookup
        Map<Long, List<HarvestRecord>> harvestsByCycleId = new LinkedHashMap<>();
        for (HarvestRecord harvest : filteredHarvests) {
            harvestsByCycleId
                    .computeIfAbsent(harvest.getCropCycle().getId(), k -> new ArrayList<>())
                    .add(harvest);
        }

        List<YieldAnalyticsResponse> results = new ArrayList<>();

        for (List<CropCycle> groupCycles : cyclesByGroup.values()) {
            CropCycle sample = groupCycles.get(0);
            String cName = sample.getCropName() != null ? sample.getCropName().trim() : "Unknown";
            String sName = (sample.getSeason() != null && !sample.getSeason().isBlank())
                    ? sample.getSeason().trim()
                    : "Unspecified";

            BigDecimal totalArea = groupCycles.stream()
                    .map(c -> safeDecimal(c.getPlot().getArea()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalHarvest = BigDecimal.ZERO;
            BigDecimal totalDamaged = BigDecimal.ZERO;
            String unit = "N/A";

            for (CropCycle cycle : groupCycles) {
                List<HarvestRecord> cycleHarvests = harvestsByCycleId.getOrDefault(cycle.getId(), List.of());
                for (HarvestRecord h : cycleHarvests) {
                    totalHarvest = totalHarvest.add(safeDecimal(h.getQuantity()));
                    totalDamaged = totalDamaged.add(safeDecimal(h.getDamagedQuantity()));
                    if ("N/A".equals(unit) && h.getQuantityUnit() != null && !h.getQuantityUnit().isBlank()) {
                        unit = h.getQuantityUnit();
                    }
                }
            }

            BigDecimal yieldPerAcre = totalArea.compareTo(BigDecimal.ZERO) > 0
                    ? totalHarvest.divide(totalArea, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            results.add(YieldAnalyticsResponse.builder()
                    .cropName(cName)
                    .season(sName)
                    .cropCycleCount(groupCycles.size())
                    .totalCultivatedArea(totalArea.setScale(2, RoundingMode.HALF_UP))
                    .totalHarvestQuantity(totalHarvest.setScale(2, RoundingMode.HALF_UP))
                    .totalDamagedQuantity(totalDamaged.setScale(2, RoundingMode.HALF_UP))
                    .yieldPerAcre(yieldPerAcre)
                    .quantityUnit(unit)
                    .build());
        }

        results.sort(Comparator.comparing(YieldAnalyticsResponse::getCropName)
                .thenComparing(YieldAnalyticsResponse::getSeason));

        return results;
    }

    @Transactional(readOnly = true)
    public List<CostProfitAnalyticsResponse> getCostProfitAnalytics(
            Long farmId,
            String cropName,
            String season,
            LocalDate startDate,
            LocalDate endDate) {

        validateFilters(farmId, startDate, endDate);
        String normalizedCrop = normalizeFilter(cropName);
        String normalizedSeason = normalizeFilter(season);

        List<CropCycle> filteredCycles = cropCycleRepository.findAll().stream()
                .filter(c -> farmId == null || c.getPlot().getFarm().getId().equals(farmId))
                .filter(c -> matchesCrop(c.getCropName(), normalizedCrop))
                .filter(c -> normalizedSeason == null || (c.getSeason() != null
                        && c.getSeason().trim().toLowerCase().contains(normalizedSeason)))
                .toList();

        // Group crop cycles by (cropName + season)
        Map<String, List<CropCycle>> cyclesByGroup = new LinkedHashMap<>();
        for (CropCycle cycle : filteredCycles) {
            String cName = cycle.getCropName() != null ? cycle.getCropName().trim() : "Unknown";
            String sName = (cycle.getSeason() != null && !cycle.getSeason().isBlank())
                    ? cycle.getSeason().trim()
                    : "Unspecified";
            String key = cName.toLowerCase() + "||" + sName.toLowerCase();
            cyclesByGroup.computeIfAbsent(key, k -> new ArrayList<>()).add(cycle);
        }

        // Pre-fetch related records once to avoid repeated DB queries
        List<SeasonBudget> allBudgets = seasonBudgetRepository.findAll();
        List<EquipmentUsage> allEquipmentUsages = equipmentUsageRepository.findAll().stream()
                .filter(e -> e.getCropCycle() != null)
                .filter(e -> isWithinDateRange(e.getUsageDate(), startDate, endDate))
                .toList();

        List<LabourAttendance> allLabour = labourAttendanceRepository.findAll().stream()
                .filter(l -> l.getCropCycle() != null)
                .filter(l -> !"ABSENT".equalsIgnoreCase(l.getStatus()))
                .filter(l -> isWithinDateRange(l.getAttendanceDate(), startDate, endDate))
                .toList();

        List<StockMovement> allStockOuts = stockMovementRepository.findAll().stream()
                .filter(s -> s.getType() == StockMovement.MovementType.OUT)
                .filter(s -> s.getCropActivity() != null && s.getCropActivity().getCropCycle() != null)
                .filter(s -> isWithinDateRange(s.getCreatedAt().toLocalDate(), startDate, endDate))
                .toList();

        // Track activity IDs already costed via StockMovement to avoid double counting
        Set<Long> stockCostedActivityIds = new HashSet<>();
        for (StockMovement s : allStockOuts) {
            stockCostedActivityIds.add(s.getCropActivity().getId());
        }

        List<CropActivity> allActivities = cropActivityRepository.findAll().stream()
                .filter(a -> a.getCropCycle() != null)
                .filter(a -> !stockCostedActivityIds.contains(a.getId()))
                .filter(a -> isWithinDateRange(a.getActivityDate(), startDate, endDate))
                .toList();

        List<CostProfitAnalyticsResponse> results = new ArrayList<>();

        for (List<CropCycle> groupCycles : cyclesByGroup.values()) {
            CropCycle sample = groupCycles.get(0);
            String cName = sample.getCropName() != null ? sample.getCropName().trim() : "Unknown";
            String sName = (sample.getSeason() != null && !sample.getSeason().isBlank())
                    ? sample.getSeason().trim()
                    : "Unspecified";

            Set<Long> cycleIds = new HashSet<>();
            for (CropCycle c : groupCycles) {
                cycleIds.add(c.getId());
            }

            BigDecimal plannedBudget = allBudgets.stream()
                    .filter(b -> cycleIds.contains(b.getCropCycle().getId()))
                    .map(b -> safeDecimal(b.getTotalBudget()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal equipmentCost = allEquipmentUsages.stream()
                    .filter(e -> cycleIds.contains(e.getCropCycle().getId()))
                    .map(e -> safeDecimal(e.getUsageCost()).add(safeDecimal(e.getFuelCost())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal labourCost = allLabour.stream()
                    .filter(l -> cycleIds.contains(l.getCropCycle().getId()))
                    .map(l -> safeDecimal(l.getDailyWage()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal inventoryCost = allStockOuts.stream()
                    .filter(s -> cycleIds.contains(s.getCropActivity().getCropCycle().getId()))
                    .map(s -> safeDecimal(s.getTotalCost()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal otherActivityCost = allActivities.stream()
                    .filter(a -> cycleIds.contains(a.getCropCycle().getId()))
                    .map(a -> safeDecimal(a.getActivityCost()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCost = equipmentCost
                    .add(labourCost)
                    .add(inventoryCost)
                    .add(otherActivityCost);

            BigDecimal budgetVariance = plannedBudget.subtract(totalCost);

            BigDecimal budgetUtilizationPercent = plannedBudget.compareTo(BigDecimal.ZERO) > 0
                    ? totalCost.multiply(BigDecimal.valueOf(100))
                            .divide(plannedBudget, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

            results.add(CostProfitAnalyticsResponse.builder()
                    .cropName(cName)
                    .season(sName)
                    .cropCycleCount(groupCycles.size())
                    .plannedBudget(plannedBudget.setScale(2, RoundingMode.HALF_UP))
                    .equipmentCost(equipmentCost.setScale(2, RoundingMode.HALF_UP))
                    .labourCost(labourCost.setScale(2, RoundingMode.HALF_UP))
                    .inventoryCost(inventoryCost.setScale(2, RoundingMode.HALF_UP))
                    .otherActivityCost(otherActivityCost.setScale(2, RoundingMode.HALF_UP))
                    .totalCost(totalCost.setScale(2, RoundingMode.HALF_UP))
                    .budgetVariance(budgetVariance.setScale(2, RoundingMode.HALF_UP))
                    .budgetUtilizationPercent(budgetUtilizationPercent)
                    .totalRevenue(null)
                    .netProfit(null)
                    .profitMarginPercent(null)
                    .revenueDataAvailable(false)
                    .build());
        }

        results.sort(Comparator.comparing(CostProfitAnalyticsResponse::getCropName)
                .thenComparing(CostProfitAnalyticsResponse::getSeason));

        return results;
    }

    @Transactional(readOnly = true)
    public WaterUsageAnalyticsResponse getWaterUsageAnalytics(
            Long farmId,
            LocalDate startDate,
            LocalDate endDate) {

        validateFilters(farmId, startDate, endDate);

        List<IrrigationSchedule> schedules = irrigationScheduleRepository.findAll().stream()
                .filter(i -> farmId == null || i.getFarm().getId().equals(farmId))
                .filter(i -> isWithinDateRange(i.getScheduledAt().toLocalDate(), startDate, endDate))
                .toList();

        BigDecimal totalWater = BigDecimal.ZERO;
        long completedCount = 0;

        Map<Long, List<IrrigationSchedule>> byFarm = new LinkedHashMap<>();
        Map<LocalDate, List<IrrigationSchedule>> byDate = new TreeMap<>();

        for (IrrigationSchedule schedule : schedules) {
            BigDecimal liters = schedule.getWaterAmountLiters() == null
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(schedule.getWaterAmountLiters());
            totalWater = totalWater.add(liters);

            if ("COMPLETED".equalsIgnoreCase(schedule.getStatus())) {
                completedCount++;
            }

            byFarm.computeIfAbsent(schedule.getFarm().getId(), k -> new ArrayList<>()).add(schedule);
            byDate.computeIfAbsent(schedule.getScheduledAt().toLocalDate(), k -> new ArrayList<>()).add(schedule);
        }

        long totalSchedules = schedules.size();
        long distinctDays = byDate.size();

        BigDecimal avgPerSchedule = totalSchedules > 0
                ? totalWater.divide(BigDecimal.valueOf(totalSchedules), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal avgPerDay = distinctDays > 0
                ? totalWater.divide(BigDecimal.valueOf(distinctDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<WaterUsageAnalyticsResponse.FarmWaterUsage> farmBreakdown = new ArrayList<>();
        for (List<IrrigationSchedule> farmSchedules : byFarm.values()) {
            Farm farm = farmSchedules.get(0).getFarm();
            BigDecimal farmWater = farmSchedules.stream()
                    .map(s -> s.getWaterAmountLiters() == null
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(s.getWaterAmountLiters()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            farmBreakdown.add(WaterUsageAnalyticsResponse.FarmWaterUsage.builder()
                    .farmId(farm.getId())
                    .farmName(farm.getName())
                    .totalWaterLiters(farmWater.setScale(2, RoundingMode.HALF_UP))
                    .scheduleCount(farmSchedules.size())
                    .build());
        }
        farmBreakdown.sort(Comparator.comparing(WaterUsageAnalyticsResponse.FarmWaterUsage::getFarmName));

        List<WaterUsageAnalyticsResponse.DailyWaterUsage> dailyTrend = new ArrayList<>();
        for (Map.Entry<LocalDate, List<IrrigationSchedule>> entry : byDate.entrySet()) {
            BigDecimal dayWater = entry.getValue().stream()
                    .map(s -> s.getWaterAmountLiters() == null
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(s.getWaterAmountLiters()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dailyTrend.add(WaterUsageAnalyticsResponse.DailyWaterUsage.builder()
                    .date(entry.getKey())
                    .totalWaterLiters(dayWater.setScale(2, RoundingMode.HALF_UP))
                    .scheduleCount(entry.getValue().size())
                    .build());
        }

        return WaterUsageAnalyticsResponse.builder()
                .totalWaterUsedLiters(totalWater.setScale(2, RoundingMode.HALF_UP))
                .totalSchedules(totalSchedules)
                .completedSchedules(completedCount)
                .averageWaterPerSchedule(avgPerSchedule)
                .averageWaterPerDay(avgPerDay)
                .farmBreakdown(farmBreakdown)
                .dailyTrend(dailyTrend)
                .build();
    }

    @Transactional(readOnly = true)
    public MachineryAnalyticsResponse getMachineryAnalytics(
            Long farmId,
            String cropName,
            LocalDate startDate,
            LocalDate endDate) {

        validateFilters(farmId, startDate, endDate);
        String normalizedCrop = normalizeFilter(cropName);

        List<EquipmentUsage> usages = equipmentUsageRepository.findAll().stream()
                .filter(e -> farmId == null || e.getEquipment().getFarm().getId().equals(farmId))
                .filter(e -> normalizedCrop == null || (e.getCropCycle() != null
                        && matchesCrop(e.getCropCycle().getCropName(), normalizedCrop)))
                .filter(e -> isWithinDateRange(e.getUsageDate(), startDate, endDate))
                .toList();

        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalFuel = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        Map<Long, List<EquipmentUsage>> byMachine = new LinkedHashMap<>();
        Map<LocalDate, List<EquipmentUsage>> byDate = new TreeMap<>();

        for (EquipmentUsage u : usages) {
            totalHours = totalHours.add(safeDecimal(u.getOperatingHours()));
            totalFuel = totalFuel.add(safeDecimal(u.getFuelConsumed()));
            totalCost = totalCost.add(safeDecimal(u.getUsageCost()).add(safeDecimal(u.getFuelCost())));

            byMachine.computeIfAbsent(u.getEquipment().getId(), k -> new ArrayList<>()).add(u);
            byDate.computeIfAbsent(u.getUsageDate(), k -> new ArrayList<>()).add(u);
        }

        long totalRecords = usages.size();
        BigDecimal avgHours = totalRecords > 0
                ? totalHours.divide(BigDecimal.valueOf(totalRecords), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        List<MachineryAnalyticsResponse.MachineUsageSummary> machineBreakdown = new ArrayList<>();
        for (List<EquipmentUsage> machineUsages : byMachine.values()) {
            Equipment eq = machineUsages.get(0).getEquipment();
            BigDecimal mHours = machineUsages.stream()
                    .map(u -> safeDecimal(u.getOperatingHours()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal mFuel = machineUsages.stream()
                    .map(u -> safeDecimal(u.getFuelConsumed()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal mCost = machineUsages.stream()
                    .map(u -> safeDecimal(u.getUsageCost()).add(safeDecimal(u.getFuelCost())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            machineBreakdown.add(MachineryAnalyticsResponse.MachineUsageSummary.builder()
                    .equipmentId(eq.getId())
                    .equipmentName(eq.getName())
                    .equipmentType(eq.getType())
                    .farmName(eq.getFarm().getName())
                    .totalOperatingHours(mHours.setScale(2, RoundingMode.HALF_UP))
                    .totalFuelConsumed(mFuel.setScale(2, RoundingMode.HALF_UP))
                    .totalCost(mCost.setScale(2, RoundingMode.HALF_UP))
                    .usageCount(machineUsages.size())
                    .build());
        }
        machineBreakdown.sort(Comparator.comparing(MachineryAnalyticsResponse.MachineUsageSummary::getEquipmentName));

        List<MachineryAnalyticsResponse.DailyMachineryUsage> dailyTrend = new ArrayList<>();
        for (Map.Entry<LocalDate, List<EquipmentUsage>> entry : byDate.entrySet()) {
            BigDecimal dHours = entry.getValue().stream()
                    .map(u -> safeDecimal(u.getOperatingHours()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal dFuel = entry.getValue().stream()
                    .map(u -> safeDecimal(u.getFuelConsumed()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal dCost = entry.getValue().stream()
                    .map(u -> safeDecimal(u.getUsageCost()).add(safeDecimal(u.getFuelCost())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dailyTrend.add(MachineryAnalyticsResponse.DailyMachineryUsage.builder()
                    .date(entry.getKey())
                    .totalOperatingHours(dHours.setScale(2, RoundingMode.HALF_UP))
                    .totalFuelConsumed(dFuel.setScale(2, RoundingMode.HALF_UP))
                    .totalCost(dCost.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        return MachineryAnalyticsResponse.builder()
                .totalOperatingHours(totalHours.setScale(2, RoundingMode.HALF_UP))
                .totalFuelConsumed(totalFuel.setScale(2, RoundingMode.HALF_UP))
                .totalMachineryCost(totalCost.setScale(2, RoundingMode.HALF_UP))
                .totalUsageRecords(totalRecords)
                .averageHoursPerRecord(avgHours)
                .machineBreakdown(machineBreakdown)
                .dailyTrend(dailyTrend)
                .build();
    }

    private void validateFilters(Long farmId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate cannot be after endDate"
            );
        }
        if (farmId != null && !farmRepository.existsById(farmId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Farm not found with id: " + farmId
            );
        }
    }

    private String normalizeFilter(String value) {
        return (value == null || value.isBlank()) ? null : value.trim().toLowerCase();
    }

    private boolean matchesCrop(String actualCrop, String filterCrop) {
        if (filterCrop == null) {
            return true;
        }
        return actualCrop != null && actualCrop.trim().toLowerCase().contains(filterCrop);
    }

    private boolean isWithinDateRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null) {
            return startDate == null && endDate == null;
        }
        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}