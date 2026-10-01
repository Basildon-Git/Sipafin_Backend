package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BankReconciliation;
import com.basiltech.sipafin.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, Long> {

    List<BankReconciliation> findByBankAccountId(Long bankAccountId);

    List<BankReconciliation> findByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    List<BankReconciliation> findByReconciliationDate(LocalDate reconciliationDate);
}