package com.example.zorvyn.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public class ChartSeriesResponse {
    private String name;
    private List<BigDecimal> values;

    public ChartSeriesResponse(String name, List<BigDecimal> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<BigDecimal> getValues() {
        return values;
    }
}

