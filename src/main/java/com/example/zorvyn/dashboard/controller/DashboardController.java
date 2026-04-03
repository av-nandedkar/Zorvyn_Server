package com.example.zorvyn.dashboard.controller;

import com.example.zorvyn.auth.security.AppUserPrincipal;
import com.example.zorvyn.common.model.RecordType;
import com.example.zorvyn.dashboard.dto.CategoryBreakdownResponse;
import com.example.zorvyn.dashboard.dto.DashboardSummaryResponse;
import com.example.zorvyn.dashboard.dto.KpiResponse;
import com.example.zorvyn.dashboard.dto.RoleInsightsResponse;
import com.example.zorvyn.dashboard.dto.TimeSeriesPointResponse;
import com.example.zorvyn.dashboard.service.DashboardService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return dashboardService.getSummary(from, to, principal);
    }

    @GetMapping("/kpis")
    public KpiResponse getKpis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return dashboardService.getKpis(from, to, principal);
    }

    @GetMapping("/timeseries")
    public List<TimeSeriesPointResponse> getTimeSeries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "MONTH") String granularity,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return dashboardService.getTimeSeries(from, to, granularity, principal);
    }

    @GetMapping("/category-breakdown")
    public List<CategoryBreakdownResponse> getCategoryBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "EXPENSE") RecordType type,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return dashboardService.getCategoryBreakdown(from, to, type, principal);
    }

    @GetMapping("/alerts")
    public List<String> getAlerts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return dashboardService.getAlerts(from, to, principal);
    }

    @GetMapping("/insights")
    public RoleInsightsResponse getRoleInsights(@AuthenticationPrincipal AppUserPrincipal principal) {
        return dashboardService.getRoleInsights(principal);
    }
}

