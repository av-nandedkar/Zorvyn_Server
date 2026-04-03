package com.example.zorvyn.finance.repository;

import com.example.zorvyn.finance.entity.BudgetSnapshot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetSnapshotRepository extends JpaRepository<BudgetSnapshot, Long> {

    List<BudgetSnapshot> findTop24ByOwner_IdOrderByMonthLabelDesc(Long ownerId);

    List<BudgetSnapshot> findTop48ByOrderByMonthLabelDesc();
}

