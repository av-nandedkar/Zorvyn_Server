package com.example.zorvyn.finance.dto;

import com.example.zorvyn.common.model.RecordType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class FinancialRecordResponse {
    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private String category;
    private LocalDate date;
    private String notes;
    private String merchant;
    private String paymentMethod;
    private String currency;
    private String tags;
    private boolean recurring;
    private String createdBy;
    private Instant createdAt;

    public FinancialRecordResponse(
        Long id,
        BigDecimal amount,
        RecordType type,
        String category,
        LocalDate date,
        String notes,
        String merchant,
        String paymentMethod,
        String currency,
        String tags,
        boolean recurring,
        String createdBy,
        Instant createdAt
    ) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.notes = notes;
        this.merchant = merchant;
        this.paymentMethod = paymentMethod;
        this.currency = currency;
        this.tags = tags;
        this.recurring = recurring;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RecordType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getNotes() {
        return notes;
    }

    public String getMerchant() {
        return merchant;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTags() {
        return tags;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}


