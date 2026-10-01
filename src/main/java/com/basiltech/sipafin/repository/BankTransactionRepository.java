package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BankLedgerTransactionType;
import com.basiltech.sipafin.model.BankTransaction;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByBankAccountId(Long bankAccountId);

    List<BankTransaction> findByBranchId(Long branchId);

    List<BankTransaction> findByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    List<BankTransaction> findByBankAccountIdAndCurrency(Long bankAccountId, CurrencyCode currency);

    List<BankTransaction> findByTransactionGroupId(String transactionGroupId);

    List<BankTransaction> findByCurrency(CurrencyCode currency);

    List<BankTransaction> findByTransactionType(BankLedgerTransactionType transactionType);

    List<BankTransaction> findByStatus(TransactionStatus status);

    List<BankTransaction> findByTransactionDateBetween(LocalDate fromDate, LocalDate toDate);
}