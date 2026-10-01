package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.ClientLoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientLoanTransactionRepository extends JpaRepository<ClientLoanTransaction, Long> {

    List<ClientLoanTransaction> findByClientLoanAccountId(Long clientLoanAccountId);

    List<ClientLoanTransaction> findByBranchId(Long branchId);
}