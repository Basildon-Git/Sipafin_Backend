package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class MoneyMovementDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CashOpeningBalanceRequest(

            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.00", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankDepositRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankWithdrawalRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BranchTransferRequest(
            @NotNull(message = "From branch ID is required")
            Long fromBranchId,

            @NotNull(message = "To branch ID is required")
            Long toBranchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ExpensePaymentRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Payment source is required")
            MoneySourceType paymentSource,

            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @NotBlank(message = "Expense category is required")
            String category,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FloatAdjustmentRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Direction is required")
            MoneyDirection direction,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankAdjustmentRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Direction is required")
            MoneyDirection direction,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountTransferRequest(
            @NotNull(message = "Source type is required")
            MoneySourceType fromType,

            Long fromBranchId,

            Long fromBankAccountId,

            @NotNull(message = "Destination type is required")
            MoneySourceType toType,

            Long toBranchId,

            Long toBankAccountId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrencyExchangeRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Received currency is required")
            CurrencyCode receivedCurrency,

            @NotNull(message = "Received amount is required")
            @DecimalMin(value = "0.01", message = "Received amount must be greater than zero")
            BigDecimal receivedAmount,

            @NotNull(message = "Received destination is required")
            MoneySourceType receivedTo,

            Long receivedBankAccountId,

            @NotNull(message = "Paid currency is required")
            CurrencyCode paidCurrency,

            @NotNull(message = "Paid amount is required")
            @DecimalMin(value = "0.01", message = "Paid amount must be greater than zero")
            BigDecimal paidAmount,

            @NotNull(message = "Exchange rate is required")
            @DecimalMin(value = "0.0001", message = "Exchange rate must be greater than zero")
            BigDecimal exchangeRate,

            @NotNull(message = "Paid from source is required")
            MoneySourceType paidFrom,

            Long paidBankAccountId,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RevenueReceiptRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Revenue source is required")
            MoneySourceType source,

            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Revenue date is required")
            LocalDate revenueDate,

            @NotNull(message = "Revenue category is required")
            RevenueCategory category,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateBranchFloatTransactionRequest(
            @NotNull(message = "Direction is required")
            MoneyDirection direction,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.00", message = "Amount cannot be negative")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @NotNull(message = "Status is required")
            TransactionStatus status,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateBankTransactionRequest(
            @NotNull(message = "Direction is required")
            MoneyDirection direction,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.00", message = "Amount cannot be negative")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @NotNull(message = "Status is required")
            TransactionStatus status,

            @Size(max = 100)
            String reference,

            @Size(max = 255)
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record BranchFloatTransactionResponse(
            Long id,
            Long branchFloatAccountId,
            Long branchId,
            String branchName,
            String branchCode,
            CurrencyCode currency,
            BranchFloatTransactionType transactionType,
            MoneyDirection direction,
            BigDecimal amount,
            BigDecimal balanceAfter,
            LocalDate transactionDate,
            String transactionGroupId,
            TransactionStatus status,
            String reference,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record BankTransactionResponse(
            Long id,
            Long bankAccountId,
            String accountName,
            String accountNumber,
            Long bankId,
            String bankName,
            String bankCode,
            Long branchId,
            String branchName,
            String branchCode,
            CurrencyCode currency,
            BankLedgerTransactionType transactionType,
            MoneyDirection direction,
            BigDecimal amount,
            BigDecimal balanceAfter,
            LocalDate transactionDate,
            String transactionGroupId,
            TransactionStatus status,
            String reference,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record MovementResponse(
            String transactionGroupId,
            BranchFloatTransactionResponse floatTransaction,
            BankTransactionResponse bankTransaction,
            CommissionDtos.CommissionResponse commission
    ) {
    }

    public record BranchTransferResponse(
            String transactionGroupId,
            BranchFloatTransactionResponse fromBranchTransaction,
            BranchFloatTransactionResponse toBranchTransaction
    ) {
    }

    public record CurrencyExchangeResponse(
            String transactionGroupId,
            BranchFloatTransactionResponse receivedFloatTransaction,
            BankTransactionResponse receivedBankTransaction,
            BranchFloatTransactionResponse paidFloatTransaction,
            BankTransactionResponse paidBankTransaction
    ) {
    }

    public record AccountTransferResponse(
            String transactionGroupId,
            BranchFloatTransactionResponse sourceFloatTransaction,
            BankTransactionResponse sourceBankTransaction,
            BranchFloatTransactionResponse destinationFloatTransaction,
            BankTransactionResponse destinationBankTransaction
    ) {
    }

    public record RevenueResponse(
            Long id,
            Long branchId,
            String branchName,
            Long bankAccountId,
            String bankName,
            String accountName,
            CurrencyCode currency,
            RevenueCategory category,
            MoneySourceType source,
            BigDecimal amount,
            LocalDate revenueDate,
            String transactionGroupId,
            RevenueStatus status,
            String reference,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record BranchCurrencyTransactionsResponse(
            Long branchId,
            CurrencyCode currency,
            java.util.List<BranchFloatTransactionResponse> floatTransactions,
            java.util.List<BankTransactionResponse> bankTransactions
    ) {
    }
}