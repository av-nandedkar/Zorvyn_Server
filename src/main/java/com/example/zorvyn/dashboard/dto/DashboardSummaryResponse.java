package com.example.zorvyn.dashboard.dto;

import com.example.zorvyn.finance.dto.FinancialRecordResponse;
import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private List<CategoryTotalResponse> categoryTotals;
    private List<MonthlyTrendResponse> monthlyTrends;
    private List<FinancialRecordResponse> recentActivity;

    public DashboardSummaryResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,
        List<CategoryTotalResponse> categoryTotals,
        List<MonthlyTrendResponse> monthlyTrends,
        List<FinancialRecordResponse> recentActivity
    ) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
        this.categoryTotals = categoryTotals;
        this.monthlyTrends = monthlyTrends;
        this.recentActivity = recentActivity;
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

    public List<CategoryTotalResponse> getCategoryTotals() {
        return categoryTotals;
    }

    public List<MonthlyTrendResponse> getMonthlyTrends() {
        return monthlyTrends;
    }

    public List<FinancialRecordResponse> getRecentActivity() {
        return recentActivity;
    }
}


