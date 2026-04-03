package com.example.zorvyn.dashboard.service;

import com.example.zorvyn.auth.security.AppUserPrincipal;
import com.example.zorvyn.common.model.RecordType;
import com.example.zorvyn.common.model.RoleType;
import com.example.zorvyn.dashboard.dto.CategoryBreakdownResponse;
import com.example.zorvyn.dashboard.dto.CategoryTotalResponse;
import com.example.zorvyn.dashboard.dto.ChartSeriesResponse;
import com.example.zorvyn.dashboard.dto.DashboardSummaryResponse;
import com.example.zorvyn.dashboard.dto.DynamicChartResponse;
import com.example.zorvyn.dashboard.dto.KpiResponse;
import com.example.zorvyn.dashboard.dto.MonthlyTrendResponse;
import com.example.zorvyn.dashboard.dto.RoleInsightsResponse;
import com.example.zorvyn.dashboard.dto.TimeSeriesPointResponse;
import com.example.zorvyn.finance.entity.BudgetSnapshot;
import com.example.zorvyn.finance.entity.CashFlowSnapshot;
import com.example.zorvyn.finance.dto.FinancialRecordResponse;
import com.example.zorvyn.finance.entity.FinancialRecord;
import com.example.zorvyn.finance.entity.InvestmentSnapshot;
import com.example.zorvyn.finance.repository.BudgetSnapshotRepository;
import com.example.zorvyn.finance.repository.CashFlowSnapshotRepository;
import com.example.zorvyn.finance.repository.FinancialRecordRepository;
import com.example.zorvyn.finance.repository.InvestmentSnapshotRepository;
import com.example.zorvyn.finance.service.FinancialRecordService;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final String GRANULARITY_MONTH = "MONTH";
    private static final String GRANULARITY_WEEK = "WEEK";
    private static final String GRANULARITY_DAY = "DAY";

    private static final String CASH_MODE_OPERATING = "operating";
    private static final String CASH_MODE_INVESTING = "investing";
    private static final String CASH_MODE_FINANCING = "financing";

    private static final String ALERT_EXPENSE_OVER_INCOME = "Expenses exceed income in the selected period.";
    private static final String ALERT_STABLE_CASHFLOW = "No critical alerts. Cash flow looks stable for this period.";

    private final FinancialRecordRepository financialRecordRepository;
    private final FinancialRecordService financialRecordService;
    private final BudgetSnapshotRepository budgetSnapshotRepository;
    private final CashFlowSnapshotRepository cashFlowSnapshotRepository;
    private final InvestmentSnapshotRepository investmentSnapshotRepository;

    public DashboardService(
            FinancialRecordRepository financialRecordRepository,
            FinancialRecordService financialRecordService,
            BudgetSnapshotRepository budgetSnapshotRepository,
            CashFlowSnapshotRepository cashFlowSnapshotRepository,
            InvestmentSnapshotRepository investmentSnapshotRepository
    ) {
        this.financialRecordRepository = financialRecordRepository;
        this.financialRecordService = financialRecordService;
        this.budgetSnapshotRepository = budgetSnapshotRepository;
        this.cashFlowSnapshotRepository = cashFlowSnapshotRepository;
        this.investmentSnapshotRepository = investmentSnapshotRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(LocalDate from, LocalDate to, AppUserPrincipal principal) {
        List<FinancialRecord> records = getRecordsInRange(from, to, principal);

        BigDecimal income = totalForType(records, RecordType.INCOME);
        BigDecimal expense = totalForType(records, RecordType.EXPENSE);
        BigDecimal net = income.subtract(expense);

        List<CategoryTotalResponse> categoryTotals = buildCategoryTotals(records);
        List<MonthlyTrendResponse> monthlyTrends = buildMonthlyTrends(records);
        List<FinancialRecord> recentRecords = isAdmin(principal)
                ? financialRecordRepository.findTop10ByOrderByDateDescCreatedAtDesc()
                : financialRecordRepository.findTop10ByCreatedBy_IdOrderByDateDescCreatedAtDesc(principal.getUser().getId());

        List<FinancialRecordResponse> recentActivity = recentRecords
                .stream()
                .map(financialRecordService::toResponse)
                .collect(Collectors.toList());

        return new DashboardSummaryResponse(income, expense, net, categoryTotals, monthlyTrends, recentActivity);
    }

    @Transactional(readOnly = true)
    public KpiResponse getKpis(LocalDate from, LocalDate to, AppUserPrincipal principal) {
        LocalDate[] dateRange = resolveRange(from, to);
        List<FinancialRecord> records = getRecordsInRange(dateRange[0], dateRange[1], principal);
        BigDecimal income = totalForType(records, RecordType.INCOME);
        BigDecimal expense = totalForType(records, RecordType.EXPENSE);
        BigDecimal net = income.subtract(expense);

        BigDecimal savingsRate = calculateSavingsRate(income, net);
        BigDecimal averageDailyExpense = calculateAverageDailyExpense(expense, dateRange[0], dateRange[1]);

        return new KpiResponse(income, expense, net, savingsRate, averageDailyExpense);
    }

    @Transactional(readOnly = true)
    public List<TimeSeriesPointResponse> getTimeSeries(LocalDate from, LocalDate to, String granularity, AppUserPrincipal principal) {
        List<FinancialRecord> records = getRecordsInRange(from, to, principal);
        String safeGranularity = granularity == null ? GRANULARITY_MONTH : granularity.trim().toUpperCase(Locale.ROOT);

        Map<String, BigDecimal> incomeByBucket = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByBucket = new LinkedHashMap<>();

        for (FinancialRecord record : records) {
            String bucket = toBucket(record.getDate(), safeGranularity);
            if (record.getType() == RecordType.INCOME) {
                incomeByBucket.merge(bucket, record.getAmount(), BigDecimal::add);
            } else {
                expenseByBucket.merge(bucket, record.getAmount(), BigDecimal::add);
            }
        }

        List<String> bucketLabels = new ArrayList<>();
        bucketLabels.addAll(incomeByBucket.keySet());
        bucketLabels.addAll(expenseByBucket.keySet());

        return bucketLabels.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .map(label -> {
                    BigDecimal income = incomeByBucket.getOrDefault(label, BigDecimal.ZERO);
                    BigDecimal expense = expenseByBucket.getOrDefault(label, BigDecimal.ZERO);
                    return new TimeSeriesPointResponse(label, income, expense, income.subtract(expense));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryBreakdownResponse> getCategoryBreakdown(LocalDate from, LocalDate to, RecordType type, AppUserPrincipal principal) {
        List<FinancialRecord> records = getRecordsInRange(from, to, principal)
                .stream()
                .filter(record -> record.getType() == type)
                .collect(Collectors.toList());

        BigDecimal grandTotal = records.stream()
                .map(FinancialRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> grouped = new LinkedHashMap<>();
        for (FinancialRecord record : records) {
            grouped.merge(record.getCategory(), record.getAmount(), BigDecimal::add);
        }

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> {
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = entry.getValue()
                                .multiply(BigDecimal.valueOf(100))
                                .divide(grandTotal, 2, RoundingMode.HALF_UP);
                    }
                    return new CategoryBreakdownResponse(entry.getKey(), entry.getValue(), percentage);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getAlerts(LocalDate from, LocalDate to, AppUserPrincipal principal) {
        LocalDate[] dateRange = resolveRange(from, to);
        List<FinancialRecord> records = getRecordsInRange(dateRange[0], dateRange[1], principal);
        BigDecimal income = totalForType(records, RecordType.INCOME);
        BigDecimal expense = totalForType(records, RecordType.EXPENSE);

        List<String> alerts = new ArrayList<>();
        if (expense.compareTo(income) > 0) {
            alerts.add(ALERT_EXPENSE_OVER_INCOME);
        }

        if (!records.isEmpty()) {
            findUnusualExpense(records).ifPresent(record ->
                    alerts.add("Unusually high expense detected in " + record.getCategory() + ": " + record.getAmount())
            );
        }

        if (alerts.isEmpty()) {
            alerts.add(ALERT_STABLE_CASHFLOW);
        }
        return alerts;
    }

    @Transactional(readOnly = true)
    public RoleInsightsResponse getRoleInsights(AppUserPrincipal principal) {
        RoleType role = principal.getUser().getRole();
        Long ownerId = principal.getUser().getId();

        List<BudgetSnapshot> budgets = isAdmin(principal)
                ? budgetSnapshotRepository.findTop48ByOrderByMonthLabelDesc()
                : budgetSnapshotRepository.findTop24ByOwner_IdOrderByMonthLabelDesc(ownerId);
        List<CashFlowSnapshot> cashFlows = isAdmin(principal)
                ? cashFlowSnapshotRepository.findTop24ByOrderByPeriodLabelDesc()
                : cashFlowSnapshotRepository.findTop12ByOwner_IdOrderByPeriodLabelDesc(ownerId);
        List<InvestmentSnapshot> investments = isAdmin(principal)
                ? investmentSnapshotRepository.findAllByOrderByCurrentValueDesc()
                : investmentSnapshotRepository.findByOwner_IdOrderByCurrentValueDesc(ownerId);

        if (role == RoleType.VIEWER) {
            return new RoleInsightsResponse(role.name(), buildViewerInsights(budgets));
        }
        if (role == RoleType.ANALYST) {
            return new RoleInsightsResponse(role.name(), buildAnalystInsights(budgets, cashFlows));
        }
        return new RoleInsightsResponse(role.name(), buildAdminInsights(budgets, cashFlows, investments));
    }

    private List<DynamicChartResponse> buildViewerInsights(List<BudgetSnapshot> budgets) {
        Map<String, BigDecimal> plannedByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> actualByMonth = new LinkedHashMap<>();
        for (BudgetSnapshot snapshot : budgets) {
            plannedByMonth.merge(snapshot.getMonthLabel(), snapshot.getPlannedAmount(), BigDecimal::add);
            actualByMonth.merge(snapshot.getMonthLabel(), snapshot.getActualAmount(), BigDecimal::add);
        }

        List<String> labels = plannedByMonth.keySet().stream().sorted().collect(Collectors.toList());
        List<BigDecimal> plannedValues = labels.stream().map(label -> plannedByMonth.getOrDefault(label, BigDecimal.ZERO)).collect(Collectors.toList());
        List<BigDecimal> actualValues = labels.stream().map(label -> actualByMonth.getOrDefault(label, BigDecimal.ZERO)).collect(Collectors.toList());

        DynamicChartResponse monthly = new DynamicChartResponse(
                "viewer-budget-monthly",
                "Budget vs Actual (Monthly)",
                "bar",
                labels,
                List.of(
                        new ChartSeriesResponse("Planned", plannedValues),
                        new ChartSeriesResponse("Actual", actualValues)
                ),
                "Track monthly discipline against your planned budget."
        );

        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        for (BudgetSnapshot snapshot : budgets) {
            categoryTotals.merge(snapshot.getCategory(), snapshot.getActualAmount(), BigDecimal::add);
        }
        List<String> catLabels = categoryTotals.keySet().stream().sorted().collect(Collectors.toList());
        List<BigDecimal> catValues = catLabels.stream().map(label -> categoryTotals.getOrDefault(label, BigDecimal.ZERO)).collect(Collectors.toList());

        DynamicChartResponse category = new DynamicChartResponse(
                "viewer-expense-category",
                "Expense Distribution by Category",
                "donut",
                catLabels,
                List.of(new ChartSeriesResponse("Spend", catValues)),
                "Your top categories highlight where optimization is possible."
        );

        return List.of(monthly, category);
    }

    private List<DynamicChartResponse> buildAnalystInsights(List<BudgetSnapshot> budgets, List<CashFlowSnapshot> cashFlows) {
        List<String> cashLabels = cashFlows.stream()
                .map(CashFlowSnapshot::getPeriodLabel)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        List<BigDecimal> operating = mapCashSeries(cashLabels, cashFlows, CASH_MODE_OPERATING);
        List<BigDecimal> investing = mapCashSeries(cashLabels, cashFlows, CASH_MODE_INVESTING);
        List<BigDecimal> financing = mapCashSeries(cashLabels, cashFlows, CASH_MODE_FINANCING);

        DynamicChartResponse cashFlowChart = new DynamicChartResponse(
                "analyst-cashflow-trend",
                "Cashflow Drivers Over Time",
                "line",
                cashLabels,
                List.of(
                        new ChartSeriesResponse("Operating", operating),
                        new ChartSeriesResponse("Investing", investing),
                        new ChartSeriesResponse("Financing", financing)
                ),
                "Operating cash trend is your primary health signal."
        );

        Map<String, BigDecimal> planned = new LinkedHashMap<>();
        Map<String, BigDecimal> actual = new LinkedHashMap<>();
        for (BudgetSnapshot snapshot : budgets) {
            planned.merge(snapshot.getCategory(), snapshot.getPlannedAmount(), BigDecimal::add);
            actual.merge(snapshot.getCategory(), snapshot.getActualAmount(), BigDecimal::add);
        }
        List<String> labels = planned.keySet().stream().sorted().collect(Collectors.toList());
        List<BigDecimal> plannedValues = labels.stream().map(label -> planned.getOrDefault(label, BigDecimal.ZERO)).collect(Collectors.toList());
        List<BigDecimal> actualValues = labels.stream().map(label -> actual.getOrDefault(label, BigDecimal.ZERO)).collect(Collectors.toList());

        DynamicChartResponse variance = new DynamicChartResponse(
                "analyst-budget-variance",
                "Category Budget Variance",
                "stacked",
                labels,
                List.of(
                        new ChartSeriesResponse("Planned", plannedValues),
                        new ChartSeriesResponse("Actual", actualValues)
                ),
                "Focus on categories where actual spend persistently exceeds plan."
        );

        return List.of(cashFlowChart, variance);
    }

    private List<DynamicChartResponse> buildAdminInsights(
            List<BudgetSnapshot> budgets,
            List<CashFlowSnapshot> cashFlows,
            List<InvestmentSnapshot> investments
    ) {
        List<DynamicChartResponse> charts = new ArrayList<>();
        charts.addAll(buildAnalystInsights(budgets, cashFlows));

        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        for (InvestmentSnapshot snapshot : investments) {
            allocation.merge(snapshot.getAssetClass(), snapshot.getCurrentValue(), BigDecimal::add);
        }
        List<String> labels = allocation.keySet().stream().sorted().collect(Collectors.toList());
        List<BigDecimal> values = labels.stream().map(label -> allocation.getOrDefault(label, BigDecimal.ZERO)).collect(Collectors.toList());

        charts.add(new DynamicChartResponse(
                "admin-portfolio-allocation",
                "Portfolio Allocation by Asset Class",
                "donut",
                labels,
                List.of(new ChartSeriesResponse("Allocation", values)),
                "Use allocation concentration to rebalance portfolio risk."
        ));
        return charts;
    }

    private List<BigDecimal> mapCashSeries(List<String> labels, List<CashFlowSnapshot> cashFlows, String mode) {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        for (CashFlowSnapshot snapshot : cashFlows) {
            if (CASH_MODE_OPERATING.equals(mode)) {
                values.put(snapshot.getPeriodLabel(), snapshot.getOperatingCash());
            } else if (CASH_MODE_INVESTING.equals(mode)) {
                values.put(snapshot.getPeriodLabel(), snapshot.getInvestingCash());
            } else {
                values.put(snapshot.getPeriodLabel(), snapshot.getFinancingCash());
            }
        }
        return labels.stream().map(label -> values.getOrDefault(label, BigDecimal.ZERO)).collect(Collectors.toList());
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal net) {
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return net
                .multiply(BigDecimal.valueOf(100))
                .divide(income, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageDailyExpense(BigDecimal totalExpense, LocalDate from, LocalDate to) {
        long dayCount = Math.max(1, to.toEpochDay() - from.toEpochDay() + 1);
        return totalExpense.divide(BigDecimal.valueOf(dayCount), 2, RoundingMode.HALF_UP);
    }

    private java.util.Optional<FinancialRecord> findUnusualExpense(List<FinancialRecord> records) {
        List<FinancialRecord> expenseRecords = records.stream()
                .filter(record -> record.getType() == RecordType.EXPENSE)
                .collect(Collectors.toList());
        if (expenseRecords.isEmpty()) {
            return java.util.Optional.empty();
        }

        BigDecimal averageExpense = expenseRecords.stream()
                .map(FinancialRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(expenseRecords.size()), 2, RoundingMode.HALF_UP);

        return expenseRecords.stream()
                .filter(record -> record.getAmount().compareTo(averageExpense.multiply(BigDecimal.valueOf(2))) > 0)
                .findFirst();
    }

    private List<FinancialRecord> getRecordsInRange(LocalDate from, LocalDate to, AppUserPrincipal principal) {
        LocalDate[] range = resolveRange(from, to);
        if (isAdmin(principal)) {
            return financialRecordRepository.findByDateBetweenOrderByDateDesc(range[0], range[1]);
        }
        return financialRecordRepository.findByDateBetweenAndCreatedBy_IdOrderByDateDesc(range[0], range[1], principal.getUser().getId());
    }

    private boolean isAdmin(AppUserPrincipal principal) {
        return principal.getUser().getRole() == RoleType.ADMIN;
    }

    private LocalDate[] resolveRange(LocalDate from, LocalDate to) {
        LocalDate safeFrom = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate safeTo = to == null ? LocalDate.now() : to;
        if (safeFrom.isAfter(safeTo)) {
            throw new IllegalArgumentException("from date cannot be after to date");
        }
        return new LocalDate[] {safeFrom, safeTo};
    }

    private String toBucket(LocalDate date, String granularity) {
        if (GRANULARITY_DAY.equals(granularity)) {
            return date.toString();
        }
        if (GRANULARITY_WEEK.equals(granularity)) {
            WeekFields weekFields = WeekFields.ISO;
            int week = date.get(weekFields.weekOfWeekBasedYear());
            return date.getYear() + "-W" + String.format("%02d", week);
        }
        return YearMonth.from(date).toString();
    }

    private BigDecimal totalForType(List<FinancialRecord> records, RecordType type) {
        return records.stream()
                .filter(record -> record.getType() == type)
                .map(FinancialRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<CategoryTotalResponse> buildCategoryTotals(List<FinancialRecord> records) {
        Map<String, BigDecimal> grouped = new LinkedHashMap<>();
        for (FinancialRecord record : records) {
            grouped.merge(record.getCategory(), record.getAmount(), BigDecimal::add);
        }

        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> new CategoryTotalResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<MonthlyTrendResponse> buildMonthlyTrends(List<FinancialRecord> records) {
        Map<YearMonth, BigDecimal> incomeByMonth = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> expenseByMonth = new LinkedHashMap<>();

        for (FinancialRecord record : records) {
            YearMonth key = YearMonth.from(record.getDate());
            if (record.getType() == RecordType.INCOME) {
                incomeByMonth.merge(key, record.getAmount(), BigDecimal::add);
            } else {
                expenseByMonth.merge(key, record.getAmount(), BigDecimal::add);
            }
        }

        List<YearMonth> months = new ArrayList<>();
        months.addAll(incomeByMonth.keySet());
        months.addAll(expenseByMonth.keySet());

        return months.stream()
                .distinct()
                .sorted(Comparator.naturalOrder())
                .map(month -> {
                    BigDecimal income = incomeByMonth.getOrDefault(month, BigDecimal.ZERO);
                    BigDecimal expense = expenseByMonth.getOrDefault(month, BigDecimal.ZERO);
                    return new MonthlyTrendResponse(month.toString(), income, expense, income.subtract(expense));
                })
                .collect(Collectors.toList());
    }
}


