package com.example.zorvyn.finance.repository;

import com.example.zorvyn.finance.entity.CashFlowSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashFlowSnapshotRepository extends JpaRepository<CashFlowSnapshot, Long> {

    List<CashFlowSnapshot> findTop12ByOwner_IdOrderByPeriodLabelDesc(Long ownerId);

    List<CashFlowSnapshot> findTop24ByOrderByPeriodLabelDesc();
}

