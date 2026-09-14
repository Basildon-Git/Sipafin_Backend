package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.TransactionDtos;

import java.util.List;

public interface TransactionPostingService {

    TransactionDtos.CashTransactionResponse openCashFloat(TransactionDtos.CashOpeningBalanceRequest request);

    TransactionDtos.TransactionGroupResponse depositCashToBank(TransactionDtos.BankDepositRequest request);

    TransactionDtos.TransactionGroupResponse withdrawBankToCash(TransactionDtos.BankWithdrawalRequest request);

    TransactionDtos.BranchTransferResponse transferCashBetweenBranches(TransactionDtos.BranchTransferRequest request);

    TransactionDtos.CashTransactionResponse adjustCashFloat(TransactionDtos.CashAdjustmentRequest request);

    TransactionDtos.BankTransactionResponse adjustBankBalance(TransactionDtos.BankAdjustmentRequest request);

    List<TransactionDtos.CashTransactionResponse> getAllCashTransactions();

    List<TransactionDtos.BankTransactionResponse> getAllBankTransactions();

    List<TransactionDtos.CashTransactionResponse> getCashTransactionsByBranch(Long branchId);

    List<TransactionDtos.BankTransactionResponse> getBankTransactionsByAccount(Long bankAccountId);

    TransactionDtos.TransactionGroupResponse getTransactionGroup(String transactionGroupId);
}
