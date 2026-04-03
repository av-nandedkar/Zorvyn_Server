package com.example.zorvyn.finance.service;

import com.example.zorvyn.common.model.RecordType;
import com.example.zorvyn.finance.entity.FinancialRecord;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class FinancialRecordSpecifications {

    private FinancialRecordSpecifications() {
    }

    public static Specification<FinancialRecord> hasType(RecordType type) {
        return (root, query, builder) -> type == null ? null : builder.equal(root.get("type"), type);
    }

    public static Specification<FinancialRecord> hasCategory(String category) {
        return (root, query, builder) ->
                category == null || category.isBlank() ? null : builder.equal(builder.lower(root.get("category")), category.toLowerCase());
    }

    public static Specification<FinancialRecord> dateOnOrAfter(LocalDate date) {
        return (root, query, builder) -> date == null ? null : builder.greaterThanOrEqualTo(root.get("date"), date);
    }

    public static Specification<FinancialRecord> dateOnOrBefore(LocalDate date) {
        return (root, query, builder) -> date == null ? null : builder.lessThanOrEqualTo(root.get("date"), date);
    }

    public static Specification<FinancialRecord> hasCreatedById(Long userId) {
        return (root, query, builder) -> userId == null ? null : builder.equal(root.get("createdBy").get("id"), userId);
    }
}

