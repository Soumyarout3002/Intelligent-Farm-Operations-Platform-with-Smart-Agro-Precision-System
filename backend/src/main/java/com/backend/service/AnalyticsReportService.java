package com.backend.service;

import com.backend.dto.analytics.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsReportService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AnalyticsService analyticsService;

    public byte[] generateExcelReport(
            Long farmId,
            String cropName,
            String season,
            LocalDate startDate,
            LocalDate endDate) {

        DashboardAnalyticsResponse dashboard =
                analyticsService.getDashboardAnalytics(farmId, cropName, startDate, endDate);
        List<YieldAnalyticsResponse> yieldList =
                analyticsService.getYieldAnalytics(farmId, cropName, season, startDate, endDate);
        List<CostProfitAnalyticsResponse> costList =
                analyticsService.getCostProfitAnalytics(farmId, cropName, season, startDate, endDate);
        WaterUsageAnalyticsResponse water =
                analyticsService.getWaterUsageAnalytics(farmId, startDate, endDate);
        MachineryAnalyticsResponse machinery =
                analyticsService.getMachineryAnalytics(farmId, cropName, startDate, endDate);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createExcelHeaderStyle(workbook);
            CellStyle titleStyle = createExcelTitleStyle(workbook);

            buildDashboardSheet(workbook, headerStyle, titleStyle, dashboard,
                    farmId, cropName, season, startDate, endDate);
            buildYieldSheet(workbook, headerStyle, yieldList);
            buildCostProfitSheet(workbook, headerStyle, costList);
            buildWaterSheet(workbook, headerStyle, water);
            buildMachinerySheet(workbook, headerStyle, machinery);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate Excel analytics report: " + ex.getMessage()
            );
        }
    }

    public byte[] generatePdfReport(
            Long farmId,
            String cropName,
            String season,
            LocalDate startDate,
            LocalDate endDate) {

        DashboardAnalyticsResponse dashboard =
                analyticsService.getDashboardAnalytics(farmId, cropName, startDate, endDate);
        List<YieldAnalyticsResponse> yieldList =
                analyticsService.getYieldAnalytics(farmId, cropName, season, startDate, endDate);
        List<CostProfitAnalyticsResponse> costList =
                analyticsService.getCostProfitAnalytics(farmId, cropName, season, startDate, endDate);
        WaterUsageAnalyticsResponse water =
                analyticsService.getWaterUsageAnalytics(farmId, startDate, endDate);
        MachineryAnalyticsResponse machinery =
                analyticsService.getMachineryAnalytics(farmId, cropName, startDate, endDate);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(document, out);
            document.open();

            com.lowagie.text.Font titleFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(27, 94, 32));
            com.lowagie.text.Font sectionFont =
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            com.lowagie.text.Font metaFont =
                    FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);

            Paragraph title = new Paragraph("FarmVerse Nexus - Agricultural Analytics Report", titleFont);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph meta = new Paragraph(
                    "Generated At: " + LocalDateTime.now().format(TIMESTAMP_FORMAT)
                            + "  |  Filters: " + formatFilters(farmId, cropName, season, startDate, endDate),
                    metaFont
            );
            meta.setSpacingAfter(14);
            document.add(meta);

            // 1. Dashboard Summary
            addPdfSectionHeading(document, "1. Dashboard Summary", sectionFont);
            PdfPTable dashTable = createPdfTable(2);
            addPdfHeaderRow(dashTable, "Metric", "Value");
            addPdfRow(dashTable, "Total Farms", String.valueOf(dashboard.getTotalFarms()));
            addPdfRow(dashTable, "Total Plots", String.valueOf(dashboard.getTotalPlots()));
            addPdfRow(dashTable, "Total Cultivated Area", formatDecimal(dashboard.getTotalCultivatedArea()));
            addPdfRow(dashTable, "Active Crop Cycles", String.valueOf(dashboard.getActiveCropCycles()));
            addPdfRow(dashTable, "Total Harvest Quantity", formatDecimal(dashboard.getTotalHarvestQuantity()));
            addPdfRow(dashTable, "Average Yield per Acre", formatDecimal(dashboard.getAverageYieldPerAcre()));
            addPdfRow(dashTable, "Total Planned Budget", formatDecimal(dashboard.getTotalPlannedBudget()));
            addPdfRow(dashTable, "Total Operational Cost", formatDecimal(dashboard.getTotalOperationalCost()));
            addPdfRow(dashTable, "  - Equipment Cost", formatDecimal(dashboard.getTotalEquipmentCost()));
            addPdfRow(dashTable, "  - Labour Cost", formatDecimal(dashboard.getTotalLabourCost()));
            addPdfRow(dashTable, "  - Inventory Cost (Stock OUT)", formatDecimal(dashboard.getTotalInventoryCost()));
            addPdfRow(dashTable, "Total Water Used (Liters)", formatDecimal(dashboard.getTotalWaterUsedLiters()));
            addPdfRow(dashTable, "Total Machinery Hours", formatDecimal(dashboard.getTotalMachineryHours()));
            document.add(dashTable);

            // 2. Yield Analytics
            addPdfSectionHeading(document, "2. Yield per Acre (By Crop & Season)", sectionFont);
            PdfPTable yieldTable = createPdfTable(7);
            addPdfHeaderRow(yieldTable, "Crop", "Season", "Cycles", "Area", "Harvest Qty", "Yield/Acre", "Unit");
            if (yieldList.isEmpty()) {
                addPdfEmptyRow(yieldTable, 7, "No yield records found for selected filters.");
            } else {
                for (YieldAnalyticsResponse y : yieldList) {
                    addPdfRow(yieldTable,
                            safeText(y.getCropName()),
                            safeText(y.getSeason()),
                            String.valueOf(y.getCropCycleCount()),
                            formatDecimal(y.getTotalCultivatedArea()),
                            formatDecimal(y.getTotalHarvestQuantity()),
                            formatDecimal(y.getYieldPerAcre()),
                            safeText(y.getQuantityUnit()));
                }
            }
            document.add(yieldTable);

            // 3. Cost & Profit Analytics
            addPdfSectionHeading(document, "3. Cost & Budget Analysis (Per Crop)", sectionFont);
            PdfPTable costTable = createPdfTable(8);
            addPdfHeaderRow(costTable, "Crop", "Season", "Budget", "Equipment", "Labour", "Inventory", "Total Cost", "Variance");
            if (costList.isEmpty()) {
                addPdfEmptyRow(costTable, 8, "No cost records found for selected filters.");
            } else {
                for (CostProfitAnalyticsResponse c : costList) {
                    addPdfRow(costTable,
                            safeText(c.getCropName()),
                            safeText(c.getSeason()),
                            formatDecimal(c.getPlannedBudget()),
                            formatDecimal(c.getEquipmentCost()),
                            formatDecimal(c.getLabourCost()),
                            formatDecimal(c.getInventoryCost()),
                            formatDecimal(c.getTotalCost()),
                            formatDecimal(c.getBudgetVariance()));
                }
            }
            document.add(costTable);

            // 4. Water Usage Summary
            addPdfSectionHeading(document, "4. Water Usage Summary", sectionFont);
            PdfPTable waterTable = createPdfTable(3);
            addPdfHeaderRow(waterTable, "Farm Name", "Schedules", "Total Water (Liters)");
            if (water.getFarmBreakdown().isEmpty()) {
                addPdfEmptyRow(waterTable, 3, "No irrigation schedules found for selected filters.");
            } else {
                for (WaterUsageAnalyticsResponse.FarmWaterUsage f : water.getFarmBreakdown()) {
                    addPdfRow(waterTable,
                            safeText(f.getFarmName()),
                            String.valueOf(f.getScheduleCount()),
                            formatDecimal(f.getTotalWaterLiters()));
                }
            }
            document.add(waterTable);

            // 5. Machinery Usage Summary
            addPdfSectionHeading(document, "5. Machinery Usage Summary", sectionFont);
            PdfPTable machTable = createPdfTable(6);
            addPdfHeaderRow(machTable, "Equipment", "Type", "Farm", "Hours", "Fuel Used", "Total Cost");
            if (machinery.getMachineBreakdown().isEmpty()) {
                addPdfEmptyRow(machTable, 6, "No machinery usage records found for selected filters.");
            } else {
                for (MachineryAnalyticsResponse.MachineUsageSummary m : machinery.getMachineBreakdown()) {
                    addPdfRow(machTable,
                            safeText(m.getEquipmentName()),
                            safeText(m.getEquipmentType()),
                            safeText(m.getFarmName()),
                            formatDecimal(m.getTotalOperatingHours()),
                            formatDecimal(m.getTotalFuelConsumed()),
                            formatDecimal(m.getTotalCost()));
                }
            }
            document.add(machTable);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to generate PDF analytics report: " + ex.getMessage()
            );
        }
    }

    // ==================== EXCEL HELPERS ====================

    private void buildDashboardSheet(
            Workbook workbook,
            CellStyle headerStyle,
            CellStyle titleStyle,
            DashboardAnalyticsResponse d,
            Long farmId,
            String cropName,
            String season,
            LocalDate startDate,
            LocalDate endDate) {

        Sheet sheet = workbook.createSheet("Dashboard Summary");
        int rowIdx = 0;

        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("FarmVerse Nexus - Agricultural Analytics Report");
        titleCell.setCellStyle(titleStyle);

        addExcelKeyValueRow(sheet, rowIdx++, "Generated At", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        addExcelKeyValueRow(sheet, rowIdx++, "Applied Filters", formatFilters(farmId, cropName, season, startDate, endDate));
        rowIdx++;

        addExcelHeaderRow(sheet, rowIdx++, headerStyle, "Metric", "Value");
        addExcelKeyValueRow(sheet, rowIdx++, "Total Farms", String.valueOf(d.getTotalFarms()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Plots", String.valueOf(d.getTotalPlots()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Cultivated Area", formatDecimal(d.getTotalCultivatedArea()));
        addExcelKeyValueRow(sheet, rowIdx++, "Active Crop Cycles", String.valueOf(d.getActiveCropCycles()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Harvest Quantity", formatDecimal(d.getTotalHarvestQuantity()));
        addExcelKeyValueRow(sheet, rowIdx++, "Average Yield per Acre", formatDecimal(d.getAverageYieldPerAcre()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Planned Budget", formatDecimal(d.getTotalPlannedBudget()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Operational Cost", formatDecimal(d.getTotalOperationalCost()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Equipment Cost", formatDecimal(d.getTotalEquipmentCost()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Labour Cost", formatDecimal(d.getTotalLabourCost()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Inventory Cost", formatDecimal(d.getTotalInventoryCost()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Water Used (Liters)", formatDecimal(d.getTotalWaterUsedLiters()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Machinery Hours", formatDecimal(d.getTotalMachineryHours()));

        autoSizeColumns(sheet, 2);
    }

    private void buildYieldSheet(Workbook workbook, CellStyle headerStyle, List<YieldAnalyticsResponse> list) {
        Sheet sheet = workbook.createSheet("Yield Analytics");
        int rowIdx = 0;
        addExcelHeaderRow(sheet, rowIdx++, headerStyle,
                "Crop Name", "Season", "Crop Cycles", "Cultivated Area",
                "Harvest Quantity", "Damaged Quantity", "Yield per Acre", "Unit");

        for (YieldAnalyticsResponse y : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(safeText(y.getCropName()));
            row.createCell(1).setCellValue(safeText(y.getSeason()));
            row.createCell(2).setCellValue(y.getCropCycleCount());
            row.createCell(3).setCellValue(formatDecimal(y.getTotalCultivatedArea()));
            row.createCell(4).setCellValue(formatDecimal(y.getTotalHarvestQuantity()));
            row.createCell(5).setCellValue(formatDecimal(y.getTotalDamagedQuantity()));
            row.createCell(6).setCellValue(formatDecimal(y.getYieldPerAcre()));
            row.createCell(7).setCellValue(safeText(y.getQuantityUnit()));
        }
        autoSizeColumns(sheet, 8);
    }

    private void buildCostProfitSheet(Workbook workbook, CellStyle headerStyle, List<CostProfitAnalyticsResponse> list) {
        Sheet sheet = workbook.createSheet("Cost & Profit");
        int rowIdx = 0;
        addExcelHeaderRow(sheet, rowIdx++, headerStyle,
                "Crop Name", "Season", "Cycles", "Planned Budget", "Equipment Cost",
                "Labour Cost", "Inventory Cost", "Other Activity Cost", "Total Cost",
                "Budget Variance", "Utilization %");

        for (CostProfitAnalyticsResponse c : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(safeText(c.getCropName()));
            row.createCell(1).setCellValue(safeText(c.getSeason()));
            row.createCell(2).setCellValue(c.getCropCycleCount());
            row.createCell(3).setCellValue(formatDecimal(c.getPlannedBudget()));
            row.createCell(4).setCellValue(formatDecimal(c.getEquipmentCost()));
            row.createCell(5).setCellValue(formatDecimal(c.getLabourCost()));
            row.createCell(6).setCellValue(formatDecimal(c.getInventoryCost()));
            row.createCell(7).setCellValue(formatDecimal(c.getOtherActivityCost()));
            row.createCell(8).setCellValue(formatDecimal(c.getTotalCost()));
            row.createCell(9).setCellValue(formatDecimal(c.getBudgetVariance()));
            row.createCell(10).setCellValue(formatDecimal(c.getBudgetUtilizationPercent()));
        }
        autoSizeColumns(sheet, 11);
    }

    private void buildWaterSheet(Workbook workbook, CellStyle headerStyle, WaterUsageAnalyticsResponse w) {
        Sheet sheet = workbook.createSheet("Water Usage");
        int rowIdx = 0;
        addExcelHeaderRow(sheet, rowIdx++, headerStyle, "Metric", "Value");
        addExcelKeyValueRow(sheet, rowIdx++, "Total Water Used (Liters)", formatDecimal(w.getTotalWaterUsedLiters()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Schedules", String.valueOf(w.getTotalSchedules()));
        addExcelKeyValueRow(sheet, rowIdx++, "Average Water per Day", formatDecimal(w.getAverageWaterPerDay()));
        rowIdx++;

        addExcelHeaderRow(sheet, rowIdx++, headerStyle, "Farm Name", "Schedule Count", "Total Water (Liters)");
        for (WaterUsageAnalyticsResponse.FarmWaterUsage f : w.getFarmBreakdown()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(safeText(f.getFarmName()));
            row.createCell(1).setCellValue(f.getScheduleCount());
            row.createCell(2).setCellValue(formatDecimal(f.getTotalWaterLiters()));
        }
        autoSizeColumns(sheet, 3);
    }

    private void buildMachinerySheet(Workbook workbook, CellStyle headerStyle, MachineryAnalyticsResponse m) {
        Sheet sheet = workbook.createSheet("Machinery Usage");
        int rowIdx = 0;
        addExcelHeaderRow(sheet, rowIdx++, headerStyle, "Metric", "Value");
        addExcelKeyValueRow(sheet, rowIdx++, "Total Operating Hours", formatDecimal(m.getTotalOperatingHours()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Fuel Consumed", formatDecimal(m.getTotalFuelConsumed()));
        addExcelKeyValueRow(sheet, rowIdx++, "Total Machinery Cost", formatDecimal(m.getTotalMachineryCost()));
        rowIdx++;

        addExcelHeaderRow(sheet, rowIdx++, headerStyle,
                "Equipment Name", "Type", "Farm Name", "Usage Count",
                "Operating Hours", "Fuel Consumed", "Total Cost");
        for (MachineryAnalyticsResponse.MachineUsageSummary item : m.getMachineBreakdown()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(safeText(item.getEquipmentName()));
            row.createCell(1).setCellValue(safeText(item.getEquipmentType()));
            row.createCell(2).setCellValue(safeText(item.getFarmName()));
            row.createCell(3).setCellValue(item.getUsageCount());
            row.createCell(4).setCellValue(formatDecimal(item.getTotalOperatingHours()));
            row.createCell(5).setCellValue(formatDecimal(item.getTotalFuelConsumed()));
            row.createCell(6).setCellValue(formatDecimal(item.getTotalCost()));
        }
        autoSizeColumns(sheet, 7);
    }

    private CellStyle createExcelHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createExcelTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private void addExcelHeaderRow(Sheet sheet, int rowIdx, CellStyle style, String... headers) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void addExcelKeyValueRow(Sheet sheet, int rowIdx, String key, String value) {
        Row row = sheet.createRow(rowIdx);
        row.createCell(0).setCellValue(key);
        row.createCell(1).setCellValue(value);
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    // ==================== PDF HELPERS ====================

    private void addPdfSectionHeading(Document doc, String text, com.lowagie.text.Font font) {
        Paragraph p = new Paragraph(text, font);
        p.setSpacingBefore(12);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private PdfPTable createPdfTable(int cols) {
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(100);
        table.setSpacingAfter(6);
        return table;
    }

    private void addPdfHeaderRow(PdfPTable table, String... headers) {
        com.lowagie.text.Font headerFont =
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(new Color(46, 125, 50));
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(cell);
        }
    }

    private void addPdfRow(PdfPTable table, String... values) {
        com.lowagie.text.Font bodyFont =
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v, bodyFont));
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    private void addPdfEmptyRow(PdfPTable table, int colSpan, String message) {
        com.lowagie.text.Font emptyFont =
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);
        PdfPCell cell = new PdfPCell(new Phrase(message, emptyFont));
        cell.setColspan(colSpan);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    // ==================== COMMON HELPERS ====================

    private String formatFilters(Long farmId, String cropName, String season, LocalDate startDate, LocalDate endDate) {
        return "farmId=" + (farmId != null ? farmId : "All")
                + ", crop=" + (cropName != null && !cropName.isBlank() ? cropName : "All")
                + ", season=" + (season != null && !season.isBlank() ? season : "All")
                + ", startDate=" + (startDate != null ? startDate : "Any")
                + ", endDate=" + (endDate != null ? endDate : "Any");
    }

    private String formatDecimal(BigDecimal val) {
        return val == null ? "0.00" : val.toPlainString();
    }

    private String safeText(String val) {
        return (val == null || val.isBlank()) ? "-" : val;
    }
}