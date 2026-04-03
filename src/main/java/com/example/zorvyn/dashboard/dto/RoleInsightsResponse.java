package com.example.zorvyn.dashboard.dto;

import java.util.List;

public class RoleInsightsResponse {
    private String role;
    private List<DynamicChartResponse> charts;

    public RoleInsightsResponse(String role, List<DynamicChartResponse> charts) {
        this.role = role;
        this.charts = charts;
    }

    public String getRole() {
        return role;
    }

    public List<DynamicChartResponse> getCharts() {
        return charts;
    }
}

