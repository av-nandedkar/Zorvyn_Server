package com.example.zorvyn.dashboard.dto;

import java.util.List;

public class DynamicChartResponse {
    private String id;
    private String title;
    private String chartType;
    private List<String> labels;
    private List<ChartSeriesResponse> series;
    private String insight;

    public DynamicChartResponse(
            String id,
            String title,
            String chartType,
            List<String> labels,
            List<ChartSeriesResponse> series,
            String insight
    ) {
        this.id = id;
        this.title = title;
        this.chartType = chartType;
        this.labels = labels;
        this.series = series;
        this.insight = insight;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getChartType() {
        return chartType;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<ChartSeriesResponse> getSeries() {
        return series;
    }

    public String getInsight() {
        return insight;
    }
}

