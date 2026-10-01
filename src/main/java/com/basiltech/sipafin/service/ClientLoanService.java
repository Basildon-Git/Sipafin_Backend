package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.ClientLoanDtos;
import com.basiltech.sipafin.model.ClientLoanStatus;
import com.basiltech.sipafin.model.CurrencyCode;

import java.util.List;

public interface ClientLoanService {

    ClientLoanDtos.ClientLoanAccountResponse issueClientLoan(ClientLoanDtos.IssueClientLoanRequest request);

    ClientLoanDtos.ClientLoanAccountResponse repayClientLoan(Long clientLoanAccountId, ClientLoanDtos.RepayClientLoanRequest request);

    List<ClientLoanDtos.ClientLoanAccountResponse> getAllClientLoans();

    ClientLoanDtos.ClientLoanAccountResponse getClientLoanById(Long id);

    List<ClientLoanDtos.ClientLoanAccountResponse> getClientLoansByBranch(Long branchId);

    List<ClientLoanDtos.ClientLoanAccountResponse> getClientLoansByBranchAndCurrency(Long branchId, CurrencyCode currency);

    List<ClientLoanDtos.ClientLoanAccountResponse> getClientLoansByStatus(ClientLoanStatus status);

    List<ClientLoanDtos.ClientLoanAccountResponse> searchClientLoans(String keyword);

    ClientLoanDtos.ClientLoanAccountResponse updateClientLoan(Long id, ClientLoanDtos.UpdateClientLoanRequest request);

    List<ClientLoanDtos.ClientLoanTransactionResponse> getClientLoanTransactions(Long clientLoanAccountId);
}