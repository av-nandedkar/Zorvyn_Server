package com.example.zorvyn.dashboard.dto;

import java.math.BigDecimal;

public class CategoryBreakdownResponse {
    private String category;
    private BigDecimal total;
    private BigDecimal percentage;

    public CategoryBreakdownResponse(String category, BigDecimal total, BigDecimal percentage) {
        this.category = category;
        this.total = total;
        this.percentage = percentage;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }
}

