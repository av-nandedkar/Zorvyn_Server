package com.example.zorvyn.dashboard.dto;

import java.math.BigDecimal;

public class TimeSeriesPointResponse {
    private String label;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal net;

    public TimeSeriesPointResponse(String label, BigDecimal income, BigDecimal expense, BigDecimal net) {
        this.label = label;
        this.income = income;
        this.expense = expense;
        this.net = net;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public BigDecimal getExpense() {
        return expense;
    }

    public BigDecimal getNet() {
        return net;
    }
}

