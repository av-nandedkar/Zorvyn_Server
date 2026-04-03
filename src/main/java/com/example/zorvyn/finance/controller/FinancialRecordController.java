package com.example.zorvyn.finance.controller;

import com.example.zorvyn.auth.security.AppUserPrincipal;
import com.example.zorvyn.common.model.RecordType;
import com.example.zorvyn.finance.dto.CreateFinancialRecordRequest;
import com.example.zorvyn.finance.dto.FinancialRecordResponse;
import com.example.zorvyn.finance.dto.UpdateFinancialRecordRequest;
import com.example.zorvyn.finance.service.FinancialRecordService;
import javax.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/records")
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;

    public FinancialRecordController(FinancialRecordService financialRecordService) {
        this.financialRecordService = financialRecordService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialRecordResponse createRecord(
            @Valid @RequestBody CreateFinancialRecordRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return financialRecordService.create(request, principal);
    }

    @GetMapping
    public Page<FinancialRecordResponse> listRecords(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return financialRecordService.findAll(type, category, startDate, endDate, pageable, principal);
    }

    @GetMapping("/{id}")
    public FinancialRecordResponse getRecordById(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        return financialRecordService.findById(id, principal);
    }

    @PutMapping("/{id}")
    public FinancialRecordResponse updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFinancialRecordRequest request,
            @AuthenticationPrincipal AppUserPrincipal principal
    ) {
        return financialRecordService.update(id, request, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRecord(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
        financialRecordService.delete(id, principal);
    }
}


