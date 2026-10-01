package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BranchFloatTransaction;
import com.basiltech.sipafin.model.BranchFloatTransactionType;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BranchFloatTransactionRepository extends JpaRepository<BranchFloatTransaction, Long> {

    List<BranchFloatTransaction> findByBranchId(Long branchId);

    List<BranchFloatTransaction> findByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    List<BranchFloatTransaction> findByBranchIdAndCurrencyAndTransactionDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate
    );


    List<BranchFloatTransaction> findByTransactionType(BranchFloatTransactionType transactionType);

    List<BranchFloatTransaction> findByStatus(TransactionStatus status);

    List<BranchFloatTransaction> findByTransactionDateBetween(LocalDate fromDate, LocalDate toDate);


    List<BranchFloatTransaction> findByBranchIdAndCurrencyAndTransactionType(
            Long branchId,
            CurrencyCode currency,
            BranchFloatTransactionType transactionType
    );

    List<BranchFloatTransaction> findByBranchIdAndCurrencyAndTransactionDateAndTransactionType(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate,
            BranchFloatTransactionType transactionType
    );

    List<BranchFloatTransaction> findByCurrency(CurrencyCode currency);

    List<BranchFloatTransaction> findByTransactionGroupId(String transactionGroupId);

}