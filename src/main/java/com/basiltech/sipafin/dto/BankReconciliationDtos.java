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
import java.util.List;

public class BankReconciliationDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StartBankReconciliationRequest(
            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Reconciliation date is required")
            LocalDate reconciliationDate,

            @NotNull(message = "Statement balance is required")
            BigDecimal statementBalance,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @NotBlank(message = "Reason is required")
            @Size(max = 255, message = "Reason must not exceed 255 characters")
            String reason,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddReconciliationItemRequest(
            @NotNull(message = "Category is required")
            ReconciliationItemCategory category,

            @NotNull(message = "Effect is required")
            ReconciliationItemEffect effect,

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

    public record BankReconciliationResponse(
            Long id,
            Long bankAccountId,
            String bankName,
            String bankCode,
            String accountName,
            String accountNumber,
            Long branchId,
            String branchName,
            CurrencyCode currency,
            LocalDate reconciliationDate,
            BigDecimal systemBalanceBefore,
            BigDecimal statementBalance,
            BigDecimal difference,
            ReconciliationDifferenceType differenceType,
            BigDecimal reconciledAmount,
            BigDecimal unreconciledAmount,
            BigDecimal systemBalanceAfter,
            String transactionGroupId,
            ReconciliationStatus status,
            String reference,
            String reason,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt,
            List<BankReconciliationItemResponse> items
    ) {
    }

    public record BankReconciliationItemResponse(
            Long id,
            Long reconciliationId,
            ReconciliationItemCategory category,
            ReconciliationItemEffect effect,
            BigDecimal amount,
            LocalDate transactionDate,
            Long bankTransactionId,
            String reference,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}