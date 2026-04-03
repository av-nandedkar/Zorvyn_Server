package com.example.zorvyn.dashboard.dto;

import java.math.BigDecimal;

public class KpiResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private BigDecimal savingsRate;
    private BigDecimal averageDailyExpense;

    public KpiResponse(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal netBalance,
            BigDecimal savingsRate,
            BigDecimal averageDailyExpense
    ) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.savingsRate = savingsRate;
        this.averageDailyExpense = averageDailyExpense;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public BigDecimal getSavingsRate() {
        return savingsRate;
    }

    public BigDecimal getAverageDailyExpense() {
        return averageDailyExpense;
    }
}

