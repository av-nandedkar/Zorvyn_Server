package com.example.zorvyn.finance.repository;

import com.example.zorvyn.finance.entity.InvestmentSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentSnapshotRepository extends JpaRepository<InvestmentSnapshot, Long> {

    List<InvestmentSnapshot> findByOwner_IdOrderByCurrentValueDesc(Long ownerId);

    List<InvestmentSnapshot> findAllByOrderByCurrentValueDesc();
}

