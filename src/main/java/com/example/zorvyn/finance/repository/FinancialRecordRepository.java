package com.example.zorvyn.finance.repository;

import com.example.zorvyn.finance.entity.FinancialRecord;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    List<FinancialRecord> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);

    List<FinancialRecord> findByDateBetweenAndCreatedBy_IdOrderByDateDesc(LocalDate from, LocalDate to, Long createdById);

    List<FinancialRecord> findTop10ByOrderByDateDescCreatedAtDesc();

    List<FinancialRecord> findTop10ByCreatedBy_IdOrderByDateDescCreatedAtDesc(Long createdById);
}

