package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.ClientLoanStatus;
import com.basiltech.sipafin.model.ClientLoanTransactionType;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.model.MoneySourceType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ClientLoanDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record IssueClientLoanRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotBlank(message = "Client name is required")
            @Size(max = 150, message = "Client name must not exceed 150 characters")
            String clientName,

            @NotBlank(message = "Client phone is required")
            @Size(max = 50, message = "Client phone must not exceed 50 characters")
            String clientPhone,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Principal amount is required")
            @DecimalMin(value = "0.01", message = "Principal amount must be greater than zero")
            BigDecimal principalAmount,

            @NotNull(message = "Interest rate is required")
            @DecimalMin(value = "0.0000", message = "Interest rate cannot be negative")
            BigDecimal interestRate,

            @NotNull(message = "Funding source is required")
            MoneySourceType fundingSource,

            Long fundingBankAccountId,

            @NotNull(message = "Date issued is required")
            LocalDate dateIssued,

            LocalDate dueDate,

            @Size(max = 100, message = "Reference must not exceed 100 characters")
            String reference,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RepayClientLoanRequest(
            @NotNull(message = "Payment destination is required")
            MoneySourceType paymentDestination,

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
    public record UpdateClientLoanRequest(
            LocalDate dueDate,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record ClientLoanAccountResponse(
            Long id,
            Long branchId,
            String branchName,
            String clientName,
            String clientPhone,
            CurrencyCode currency,
            BigDecimal principalAmount,
            BigDecimal interestRate,
            BigDecimal interestAmount,
            BigDecimal totalReceivable,
            BigDecimal outstandingBalance,
            MoneySourceType fundingSource,
            Long fundingBankAccountId,
            String fundingBankAccountName,
            ClientLoanStatus status,
            LocalDate dateIssued,
            LocalDate dueDate,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record ClientLoanTransactionResponse(
            Long id,
            Long clientLoanAccountId,
            Long branchId,
            String branchName,
            CurrencyCode currency,
            ClientLoanTransactionType transactionType,
            BigDecimal amount,
            LocalDate transactionDate,
            String transactionGroupId,
            String reference,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}