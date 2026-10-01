package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.CommissionStatus;
import com.basiltech.sipafin.model.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class CommissionDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateBankCommissionAccountRequest(
            @NotNull(message = "Bank account ID is required")
            Long bankAccountId,

            @NotNull(message = "Commission rate is required")
            @DecimalMin(value = "0.0000", message = "Commission rate cannot be negative")
            BigDecimal commissionRate,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateBankCommissionAccountRequest(
            @NotNull(message = "Commission rate is required")
            @DecimalMin(value = "0.0000", message = "Commission rate cannot be negative")
            BigDecimal commissionRate,

            boolean active,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record BankCommissionAccountResponse(
            Long id,
            Long bankAccountId,
            String bankName,
            String bankCode,
            String accountName,
            String accountNumber,
            CurrencyCode currency,
            BigDecimal commissionRate,
            BigDecimal currentBalance,
            boolean active,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record CommissionResponse(
            Long id,
            Long branchId,
            String branchName,
            Long bankAccountId,
            String bankName,
            String accountName,
            Long commissionAccountId,
            CurrencyCode currency,
            BigDecimal baseAmount,
            BigDecimal commissionRate,
            BigDecimal commissionAmount,
            LocalDate commissionDate,
            String transactionGroupId,
            CommissionStatus status,
            String reference,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}