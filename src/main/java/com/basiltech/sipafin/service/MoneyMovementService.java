package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.MoneyMovementDtos;
import com.basiltech.sipafin.model.CurrencyCode;

import java.time.LocalDate;
import java.util.List;

public interface MoneyMovementService {

    MoneyMovementDtos.BranchFloatTransactionResponse openFloat(
            MoneyMovementDtos.CashOpeningBalanceRequest request
    );

    MoneyMovementDtos.MovementResponse depositFloatToBank(
            MoneyMovementDtos.BankDepositRequest request
    );

    MoneyMovementDtos.MovementResponse withdrawBankToFloat(
            MoneyMovementDtos.BankWithdrawalRequest request
    );

    MoneyMovementDtos.BranchTransferResponse transferFloatBetweenBranches(
            MoneyMovementDtos.BranchTransferRequest request
    );

    MoneyMovementDtos.BranchFloatTransactionResponse adjustFloat(
            MoneyMovementDtos.FloatAdjustmentRequest request
    );

    MoneyMovementDtos.BankTransactionResponse adjustBank(
            MoneyMovementDtos.BankAdjustmentRequest request
    );

    MoneyMovementDtos.CurrencyExchangeResponse exchangeCurrency(
            MoneyMovementDtos.CurrencyExchangeRequest request
    );

    MoneyMovementDtos.BranchFloatTransactionResponse payExpenseFromFloat(
            MoneyMovementDtos.ExpensePaymentRequest request
    );

    MoneyMovementDtos.BankTransactionResponse payExpenseFromBank(
            MoneyMovementDtos.ExpensePaymentRequest request
    );

    List<MoneyMovementDtos.BranchFloatTransactionResponse> getFloatTransactionsByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    );

    List<MoneyMovementDtos.BankTransactionResponse> getBankTransactionsByAccountAndCurrency(
            Long bankAccountId,
            CurrencyCode currency
    );

    List<MoneyMovementDtos.BranchFloatTransactionResponse> getFloatTransactionsByBranchCurrencyAndDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate
    );

    List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanReceivedFloatMovements(
            Long branchId,
            CurrencyCode currency
    );

    List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanRepaymentFloatMovements(
            Long branchId,
            CurrencyCode currency
    );

    List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanReceivedFloatMovementsByDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate
    );

    List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanRepaymentFloatMovementsByDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate
    );

    List<MoneyMovementDtos.BankTransactionResponse> getBankTransactionsByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    );
}