package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.LoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long> {

    List<LoanTransaction> findByLoanAccountId(Long loanAccountId);

    List<LoanTransaction> findByBranchId(Long branchId);
}