package com.example.zorvyn.finance.entity;

import com.example.zorvyn.user.entity.AppUser;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "cashflow_snapshots")
public class CashFlowSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String periodLabel;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal operatingCash;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal investingCash;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal financingCash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public BigDecimal getOperatingCash() {
        return operatingCash;
    }

    public void setOperatingCash(BigDecimal operatingCash) {
        this.operatingCash = operatingCash;
    }

    public BigDecimal getInvestingCash() {
        return investingCash;
    }

    public void setInvestingCash(BigDecimal investingCash) {
        this.investingCash = investingCash;
    }

    public BigDecimal getFinancingCash() {
        return financingCash;
    }

    public void setFinancingCash(BigDecimal financingCash) {
        this.financingCash = financingCash;
    }

    public AppUser getOwner() {
        return owner;
    }

    public void setOwner(AppUser owner) {
        this.owner = owner;
    }
}

