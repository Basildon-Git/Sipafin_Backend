package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BankTransaction;
import com.basiltech.sipafin.model.BankTransactionType;
import com.basiltech.sipafin.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByBankAccountId(Long bankAccountId);

    List<BankTransaction> findByBranchId(Long branchId);

    List<BankTransaction> findByTransactionGroupId(String transactionGroupId);

    List<BankTransaction> findByTransactionType(BankTransactionType transactionType);

    List<BankTransaction> findByStatus(TransactionStatus status);

    List<BankTransaction> findByTransactionDateBetween(LocalDate fromDate, LocalDate toDate);
}