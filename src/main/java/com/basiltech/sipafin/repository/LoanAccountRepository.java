package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.LoanAccount;
import com.basiltech.sipafin.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {

    List<LoanAccount> findByInvestorId(Long investorId);

    List<LoanAccount> findByBranchId(Long branchId);

    List<LoanAccount> findByStatus(LoanStatus status);

    List<LoanAccount> findByInvestorFullNameContainingIgnoreCase(String investorName);
}