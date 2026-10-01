package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.Commission;
import com.basiltech.sipafin.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CommissionRepository extends JpaRepository<Commission, Long> {

    List<Commission> findByBranchId(Long branchId);

    List<Commission> findByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    List<Commission> findByBranchIdAndCurrencyAndCommissionDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate commissionDate
    );

    List<Commission> findByBankAccountId(Long bankAccountId);

    List<Commission> findByTransactionGroupId(String transactionGroupId);
}