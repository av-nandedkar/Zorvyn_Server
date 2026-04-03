package com.example.zorvyn.config;

import com.example.zorvyn.common.model.RecordType;
import com.example.zorvyn.common.model.RoleType;
import com.example.zorvyn.common.model.UserStatus;
import com.example.zorvyn.finance.entity.BudgetSnapshot;
import com.example.zorvyn.finance.entity.CashFlowSnapshot;
import com.example.zorvyn.finance.entity.FinancialRecord;
import com.example.zorvyn.finance.entity.InvestmentSnapshot;
import com.example.zorvyn.finance.repository.BudgetSnapshotRepository;
import com.example.zorvyn.finance.repository.CashFlowSnapshotRepository;
import com.example.zorvyn.finance.repository.FinancialRecordRepository;
import com.example.zorvyn.finance.repository.InvestmentSnapshotRepository;
import com.example.zorvyn.user.entity.AppUser;
import com.example.zorvyn.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final BudgetSnapshotRepository budgetSnapshotRepository;
    private final CashFlowSnapshotRepository cashFlowSnapshotRepository;
    private final InvestmentSnapshotRepository investmentSnapshotRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            AppUserRepository userRepository,
            FinancialRecordRepository recordRepository,
            BudgetSnapshotRepository budgetSnapshotRepository,
            CashFlowSnapshotRepository cashFlowSnapshotRepository,
            InvestmentSnapshotRepository investmentSnapshotRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
        this.budgetSnapshotRepository = budgetSnapshotRepository;
        this.cashFlowSnapshotRepository = cashFlowSnapshotRepository;
        this.investmentSnapshotRepository = investmentSnapshotRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        AppUser admin = ensureUser("Aarav Admin", "admin@zorvyn.local", "Admin@123", RoleType.ADMIN);
        AppUser analyst = ensureUser("Niya Analyst", "analyst@zorvyn.local", "Analyst@123", RoleType.ANALYST);
        AppUser viewer = ensureUser("Vihaan Viewer", "viewer@zorvyn.local", "Viewer@123", RoleType.VIEWER);

        if (recordRepository.count() < 50) {
            seedRecords(admin, analyst, viewer);
        }
        if (budgetSnapshotRepository.count() < 10 || cashFlowSnapshotRepository.count() < 10 || investmentSnapshotRepository.count() < 5) {
            seedInsightsData(admin, analyst, viewer);
        }
    }

    private AppUser ensureUser(String name, String email, String rawPassword, RoleType role) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            AppUser user = new AppUser();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setStatus(UserStatus.ACTIVE);
            return userRepository.save(user);
        });
    }

    private void seedRecords(AppUser admin, AppUser analyst, AppUser viewer) {
        List<FinancialRecord> records = new ArrayList<>();
        records.addAll(generateRecords(admin, 210, 1401L));
        records.addAll(generateRecords(analyst, 180, 2201L));
        records.addAll(generateRecords(viewer, 60, 3301L));
        recordRepository.saveAll(records);
    }

    private List<FinancialRecord> generateRecords(AppUser owner, int daysBack, long seed) {
        List<FinancialRecord> records = new ArrayList<>();
        String[] expenseCategories = {"Rent", "Groceries", "Utilities", "Transport", "Insurance", "Healthcare", "Dining", "Shopping"};
        String[] incomeCategories = {"Salary", "Consulting", "Bonus", "Dividend"};
        Random random = new Random(seed);
        LocalDate today = LocalDate.now();

        for (int i = daysBack; i >= 1; i--) {
            LocalDate date = today.minusDays(i);

            if (date.getDayOfMonth() == 1) {
                records.add(record(new BigDecimal("175000.00"), RecordType.INCOME, "Salary", date, "Primary salary credit", owner));
            }
            if (date.getDayOfMonth() == 15) {
                records.add(record(new BigDecimal("22000.00"), RecordType.INCOME, incomeCategories[random.nextInt(incomeCategories.length)], date, "Variable income", owner));
            }

            BigDecimal baseExpense = BigDecimal.valueOf(700 + random.nextInt(6400));
            String category = expenseCategories[random.nextInt(expenseCategories.length)];
            records.add(record(baseExpense, RecordType.EXPENSE, category, date, "Auto-generated seeded transaction", owner));

            if (random.nextInt(10) == 0) {
                records.add(record(baseExpense.multiply(BigDecimal.valueOf(1.8)), RecordType.EXPENSE, "Travel", date, "Higher seasonal spend", owner));
            }
        }

        return records;
    }

    private FinancialRecord record(BigDecimal amount, RecordType type, String category, LocalDate date, String notes, AppUser user) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(amount);
        record.setType(type);
        record.setCategory(category);
        record.setDate(date);
        record.setNotes(notes);
        record.setMerchant("Seeded " + category + " Vendor");
        record.setPaymentMethod(type == RecordType.INCOME ? "BANK_TRANSFER" : "CARD");
        record.setCurrency("INR");
        record.setTags(type == RecordType.INCOME ? "income,seeded" : "expense,seeded");
        record.setRecurring("Rent".equalsIgnoreCase(category) || "Salary".equalsIgnoreCase(category));
        record.setCreatedBy(user);
        return record;
    }

    private void seedInsightsData(AppUser admin, AppUser analyst, AppUser viewer) {
        List<BudgetSnapshot> budgets = new ArrayList<>();
        List<CashFlowSnapshot> cashFlows = new ArrayList<>();
        List<InvestmentSnapshot> investments = new ArrayList<>();

        budgets.addAll(generateBudgetSnapshots(admin, 12, 4101L, BigDecimal.valueOf(1.4)));
        budgets.addAll(generateBudgetSnapshots(analyst, 9, 5101L, BigDecimal.valueOf(1.0)));
        budgets.addAll(generateBudgetSnapshots(viewer, 6, 6101L, BigDecimal.valueOf(0.6)));

        cashFlows.addAll(generateCashFlows(admin, 12, 4201L, BigDecimal.valueOf(2.2)));
        cashFlows.addAll(generateCashFlows(analyst, 9, 5201L, BigDecimal.valueOf(1.2)));
        cashFlows.addAll(generateCashFlows(viewer, 6, 6201L, BigDecimal.valueOf(0.7)));

        investments.addAll(generateInvestments(admin, BigDecimal.valueOf(1.8), 4301L));
        investments.addAll(generateInvestments(analyst, BigDecimal.valueOf(1.1), 5301L));
        investments.addAll(generateInvestments(viewer, BigDecimal.valueOf(0.7), 6301L));

        budgetSnapshotRepository.saveAll(budgets);
        cashFlowSnapshotRepository.saveAll(cashFlows);
        investmentSnapshotRepository.saveAll(investments);
    }

    private List<BudgetSnapshot> generateBudgetSnapshots(AppUser owner, int monthsBack, long seed, BigDecimal scale) {
        String[] categories = {"Rent", "Utilities", "Groceries", "Transport", "Healthcare", "Learning"};
        Random random = new Random(seed);
        List<BudgetSnapshot> snapshots = new ArrayList<>();

        for (int i = monthsBack; i >= 1; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            for (String category : categories) {
                BudgetSnapshot snapshot = new BudgetSnapshot();
                BigDecimal planned = BigDecimal.valueOf(3000 + random.nextInt(7000)).multiply(scale).setScale(2, java.math.RoundingMode.HALF_UP);
                BigDecimal actual = planned.multiply(BigDecimal.valueOf(0.85 + (random.nextDouble() * 0.45))).setScale(2, java.math.RoundingMode.HALF_UP);
                snapshot.setMonthLabel(ym.toString());
                snapshot.setCategory(category);
                snapshot.setPlannedAmount(planned);
                snapshot.setActualAmount(actual);
                snapshot.setOwner(owner);
                snapshots.add(snapshot);
            }
        }
        return snapshots;
    }

    private List<CashFlowSnapshot> generateCashFlows(AppUser owner, int monthsBack, long seed, BigDecimal scale) {
        List<CashFlowSnapshot> snapshots = new ArrayList<>();
        Random random = new Random(seed);

        for (int i = monthsBack; i >= 1; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            CashFlowSnapshot snapshot = new CashFlowSnapshot();
            snapshot.setPeriodLabel(ym.toString());
            snapshot.setOperatingCash(BigDecimal.valueOf(12000 + random.nextInt(24000)).multiply(scale).setScale(2, java.math.RoundingMode.HALF_UP));
            snapshot.setInvestingCash(BigDecimal.valueOf(-8000 + random.nextInt(9000)).multiply(scale).setScale(2, java.math.RoundingMode.HALF_UP));
            snapshot.setFinancingCash(BigDecimal.valueOf(-3000 + random.nextInt(7000)).multiply(scale).setScale(2, java.math.RoundingMode.HALF_UP));
            snapshot.setOwner(owner);
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    private List<InvestmentSnapshot> generateInvestments(AppUser owner, BigDecimal scale, long seed) {
        String[][] assets = {
                {"Nifty 50 ETF", "Equity"},
                {"Corporate Bond Fund", "Debt"},
                {"Gold ETF", "Commodity"},
                {"REIT Growth", "RealEstate"},
                {"US Tech ETF", "International"}
        };
        Random random = new Random(seed);
        List<InvestmentSnapshot> snapshots = new ArrayList<>();

        for (String[] asset : assets) {
            InvestmentSnapshot snapshot = new InvestmentSnapshot();
            snapshot.setAssetName(asset[0]);
            snapshot.setAssetClass(asset[1]);
            snapshot.setCurrentValue(BigDecimal.valueOf(15000 + random.nextInt(85000)).multiply(scale).setScale(2, java.math.RoundingMode.HALF_UP));
            snapshot.setChangePercent(BigDecimal.valueOf(-4 + random.nextInt(18)).setScale(2, java.math.RoundingMode.HALF_UP));
            snapshot.setOwner(owner);
            snapshots.add(snapshot);
        }
        return snapshots;
    }
}

