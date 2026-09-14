package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.PettyCashTransaction;
import com.basiltech.sipafin.model.PettyCashTransactionType;
import com.basiltech.sipafin.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PettyCashTransactionRepository extends JpaRepository<PettyCashTransaction, Long> {

    List<PettyCashTransaction> findByBranchId(Long branchId);

    List<PettyCashTransaction> findByTransactionGroupId(String transactionGroupId);

    List<PettyCashTransaction> findByTransactionType(PettyCashTransactionType transactionType);

    List<PettyCashTransaction> findByStatus(TransactionStatus status);

    List<PettyCashTransaction> findByTransactionDateBetween(LocalDate fromDate, LocalDate toDate);
}