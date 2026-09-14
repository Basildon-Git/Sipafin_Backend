package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.LoanDtos;
import com.basiltech.sipafin.model.LoanStatus;

import java.util.List;

public interface LoanService {

    LoanDtos.LoanAccountResponse receiveLoan(LoanDtos.ReceiveLoanRequest request);

    LoanDtos.LoanAccountResponse repayLoan(Long loanAccountId, LoanDtos.RepayLoanRequest request);

    List<LoanDtos.LoanAccountResponse> getAllLoans();

    List<LoanDtos.LoanAccountResponse> getLoansByInvestor(Long investorId);

    List<LoanDtos.LoanAccountResponse> getLoansByBranch(Long branchId);

    List<LoanDtos.LoanAccountResponse> getLoansByStatus(LoanStatus status);

    List<LoanDtos.LoanAccountResponse> searchLoansByInvestorName(String keyword);

    LoanDtos.LoanAccountResponse getLoanById(Long id);

    LoanDtos.LoanAccountResponse updateLoan(Long id, LoanDtos.UpdateLoanRequest request);

    List<LoanDtos.LoanTransactionResponse> getLoanTransactions(Long loanAccountId);
}