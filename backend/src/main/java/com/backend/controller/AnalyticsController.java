package com.backend.controller;

import com.backend.dto.analytics.DashboardAnalyticsResponse;
import com.backend.entity.User;
import com.backend.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.backend.dto.analytics.YieldAnalyticsResponse;
import java.util.List;
import com.backend.dto.analytics.CostProfitAnalyticsResponse;
import com.backend.dto.analytics.MachineryAnalyticsResponse;
import com.backend.dto.analytics.WaterUsageAnalyticsResponse;
import com.backend.service.AnalyticsReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsReportService analyticsReportService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardAnalyticsResponse> getDashboardAnalytics(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {

        getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                analyticsService.getDashboardAnalytics(farmId, cropName, startDate, endDate)
        );
    }

    @GetMapping("/yield")
    public ResponseEntity<List<YieldAnalyticsResponse>> getYieldAnalytics(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {

        getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                analyticsService.getYieldAnalytics(farmId, cropName, season, startDate, endDate)
        );
    }

    @GetMapping("/cost-profit")
    public ResponseEntity<List<CostProfitAnalyticsResponse>> getCostProfitAnalytics(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {

        getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                analyticsService.getCostProfitAnalytics(farmId, cropName, season, startDate, endDate)
        );
    }

    @GetMapping("/water-usage")
    public ResponseEntity<WaterUsageAnalyticsResponse> getWaterUsageAnalytics(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {

        getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                analyticsService.getWaterUsageAnalytics(farmId, startDate, endDate)
        );
    }

    @GetMapping("/machinery")
    public ResponseEntity<MachineryAnalyticsResponse> getMachineryAnalytics(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {

        getAuthenticatedUser(httpRequest);

        return ResponseEntity.ok(
                analyticsService.getMachineryAnalytics(farmId, cropName, startDate, endDate)
        );
    }

    @GetMapping("/reports/excel")
    public ResponseEntity<byte[]> downloadExcelReport(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {

        getAuthenticatedUser(httpRequest);

        byte[] excelBytes = analyticsReportService.generateExcelReport(
                farmId, cropName, season, startDate, endDate
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"farmverse-analytics-report.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/reports/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(
            @RequestParam(required = false) Long farmId,
            @RequestParam(required = false) String cropName,
            @RequestParam(required = false) String season,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {

        getAuthenticatedUser(httpRequest);

        byte[] pdfBytes = analyticsReportService.generatePdfReport(
                farmId, cropName, season, startDate, endDate
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"farmverse-analytics-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private User getAuthenticatedUser(HttpServletRequest request) {
        Object value = request.getAttribute("authenticatedUser");
        if (!(value instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }
        return user;
    }
}