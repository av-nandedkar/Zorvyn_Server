package com.example.zorvyn.finance.service;

import com.example.zorvyn.auth.security.AppUserPrincipal;
import com.example.zorvyn.common.exception.ResourceNotFoundException;
import com.example.zorvyn.common.model.RecordType;
import com.example.zorvyn.common.model.RoleType;
import com.example.zorvyn.finance.dto.CreateFinancialRecordRequest;
import com.example.zorvyn.finance.dto.FinancialRecordResponse;
import com.example.zorvyn.finance.dto.UpdateFinancialRecordRequest;
import com.example.zorvyn.finance.entity.FinancialRecord;
import com.example.zorvyn.finance.repository.FinancialRecordRepository;
import java.time.LocalDate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;

    public FinancialRecordService(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    @Transactional
    public FinancialRecordResponse create(CreateFinancialRecordRequest request, AppUserPrincipal principal) {
        FinancialRecord record = new FinancialRecord();
        applyCreateRequest(record, request);
        record.setCreatedBy(principal.getUser());
        return toResponse(financialRecordRepository.save(record));
    }

    @Transactional(readOnly = true)
    public Page<FinancialRecordResponse> findAll(
            RecordType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable,
            AppUserPrincipal principal
    ) {
        Specification<FinancialRecord> specification = buildSpecification(type, category, startDate, endDate, principal);

        return financialRecordRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public FinancialRecordResponse findById(Long id, AppUserPrincipal principal) {
        FinancialRecord record = getByIdOrThrow(id);
        requireRecordAccess(record, principal);
        return toResponse(record);
    }

    @Transactional
    public FinancialRecordResponse update(Long id, UpdateFinancialRecordRequest request, AppUserPrincipal principal) {
        FinancialRecord record = getByIdOrThrow(id);
        requireRecordAccess(record, principal);
        applyUpdateRequest(record, request);
        return toResponse(financialRecordRepository.save(record));
    }

    @Transactional
    public void delete(Long id, AppUserPrincipal principal) {
        FinancialRecord record = getByIdOrThrow(id);
        requireRecordAccess(record, principal);
        financialRecordRepository.delete(record);
    }

    public FinancialRecord getByIdOrThrow(Long id) {
        return financialRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Financial record not found with id " + id));
    }

    public FinancialRecordResponse toResponse(FinancialRecord record) {
        return new FinancialRecordResponse(
                record.getId(),
                record.getAmount(),
                record.getType(),
                record.getCategory(),
                record.getDate(),
                record.getNotes(),
                record.getMerchant(),
                record.getPaymentMethod(),
                record.getCurrency(),
                record.getTags(),
                record.isRecurring(),
                record.getCreatedBy().getEmail(),
                record.getCreatedAt()
        );
    }

    private Specification<FinancialRecord> buildSpecification(
            RecordType type,
            String category,
            LocalDate startDate,
            LocalDate endDate,
            AppUserPrincipal principal
    ) {
        Long ownerId = isAdmin(principal) ? null : principal.getUser().getId();
        return Specification
                .where(FinancialRecordSpecifications.hasType(type))
                .and(FinancialRecordSpecifications.hasCategory(category))
                .and(FinancialRecordSpecifications.dateOnOrAfter(startDate))
                .and(FinancialRecordSpecifications.dateOnOrBefore(endDate))
                .and(FinancialRecordSpecifications.hasCreatedById(ownerId));
    }

    private void applyCreateRequest(FinancialRecord record, CreateFinancialRecordRequest request) {
        applyValues(
                record,
                request.getAmount(),
                request.getType(),
                request.getCategory(),
                request.getDate(),
                request.getNotes(),
                request.getMerchant(),
                request.getPaymentMethod(),
                request.getCurrency(),
                request.getTags(),
                request.isRecurring()
        );
    }

    private void applyUpdateRequest(FinancialRecord record, UpdateFinancialRecordRequest request) {
        applyValues(
                record,
                request.getAmount(),
                request.getType(),
                request.getCategory(),
                request.getDate(),
                request.getNotes(),
                request.getMerchant(),
                request.getPaymentMethod(),
                request.getCurrency(),
                request.getTags(),
                request.isRecurring()
        );
    }

    private void applyValues(
            FinancialRecord record,
            java.math.BigDecimal amount,
            RecordType type,
            String category,
            LocalDate date,
            String notes,
            String merchant,
            String paymentMethod,
            String currency,
            String tags,
            boolean recurring
    ) {
        record.setAmount(amount);
        record.setType(type);
        record.setCategory(category.trim());
        record.setDate(date);
        record.setNotes(notes == null ? null : notes.trim());
        record.setMerchant(merchant == null ? null : merchant.trim());
        record.setPaymentMethod(paymentMethod == null ? null : paymentMethod.trim());
        record.setCurrency((currency == null || currency.isBlank()) ? "INR" : currency.trim().toUpperCase());
        record.setTags(tags == null ? null : tags.trim());
        record.setRecurring(recurring);
    }

    private boolean isAdmin(AppUserPrincipal principal) {
        return principal.getUser().getRole() == RoleType.ADMIN;
    }

    private void requireRecordAccess(FinancialRecord record, AppUserPrincipal principal) {
        if (isAdmin(principal)) {
            return;
        }
        if (!record.getCreatedBy().getId().equals(principal.getUser().getId())) {
            throw new AccessDeniedException("You can only access your own records");
        }
    }
}


