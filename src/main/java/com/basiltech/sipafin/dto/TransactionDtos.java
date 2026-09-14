package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.BankTransactionType;
import com.basiltech.sipafin.model.CashDirection;
import com.basiltech.sipafin.model.PettyCashTransactionType;
import com.basiltech.sipafin.model.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class TransactionDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CashOpeningBalanceRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankDepositRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankWithdrawalRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @Size(max = 255, message = "Description must not exceed 255 characters")
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

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CashAdjustmentRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Direction is required")
            CashDirection direction,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankAdjustmentRequest(
            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            Long branchId,

            @NotNull(message = "Direction is required")
            CashDirection direction,

            @NotNull(message = "Amount is required")
            @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
            BigDecimal amount,

            @NotNull(message = "Transaction date is required")
            LocalDate transactionDate,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record CashTransactionResponse(
            Long id,
            Long branchId,
            String branchName,
            PettyCashTransactionType transactionType,
            CashDirection direction,
            BigDecimal amount,
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
            Long branchId,
            String branchName,
            BankTransactionType transactionType,
            CashDirection direction,
            BigDecimal amount,
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

    public record TransactionGroupResponse(
            String transactionGroupId,
            CashTransactionResponse cashTransaction,
            BankTransactionResponse bankTransaction
    ) {
    }

    public record BranchTransferResponse(
            String transactionGroupId,
            CashTransactionResponse transferOut,
            CashTransactionResponse transferIn
    ) {
    }
}