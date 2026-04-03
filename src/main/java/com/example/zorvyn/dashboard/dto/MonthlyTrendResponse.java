package com.example.zorvyn.dashboard.dto;

import java.math.BigDecimal;

public class MonthlyTrendResponse {
	private String month;
	private BigDecimal income;
	private BigDecimal expense;
	private BigDecimal net;

	public MonthlyTrendResponse(String month, BigDecimal income, BigDecimal expense, BigDecimal net) {
		this.month = month;
		this.income = income;
		this.expense = expense;
		this.net = net;
	}

	public String getMonth() {
		return month;
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


