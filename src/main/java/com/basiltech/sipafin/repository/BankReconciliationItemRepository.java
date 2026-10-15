package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BankReconciliationItem;
import com.basiltech.sipafin.model.ReconciliationItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankReconciliationItemRepository extends JpaRepository<BankReconciliationItem, Long> {

    List<BankReconciliationItem> findByReconciliationId(Long reconciliationId);

    List<BankReconciliationItem> findByCategory(ReconciliationItemCategory category);
}