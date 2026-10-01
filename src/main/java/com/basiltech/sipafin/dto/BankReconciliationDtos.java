package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.model.ReconciliationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class BankReconciliationDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReconcileBankAccountRequest(
            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Reconciliation date is required")
            LocalDate reconciliationDate,

            @NotNull(message = "Statement balance is required")
            BigDecimal statementBalance,

            boolean applyAdjustment,

            String reference,

            @NotBlank(message = "Reason is required")
            String reason,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record BankReconciliationResponse(
            Long id,
            Long bankAccountId,
            String bankName,
            String accountName,
            Long branchId,
            String branchName,
            CurrencyCode currency,
            LocalDate reconciliationDate,
            BigDecimal systemBalanceBefore,
            BigDecimal statementBalance,
            BigDecimal difference,
            boolean adjustmentApplied,
            BigDecimal systemBalanceAfter,
            String transactionGroupId,
            ReconciliationStatus status,
            String reference,
            String reason,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}