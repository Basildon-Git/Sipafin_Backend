package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.LoanStatus;
import com.basiltech.sipafin.model.LoanTransactionType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class LoanDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReceiveLoanRequest(
            @NotNull(message = "Investor ID is required")
            Long investorId,

            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Principal amount is required")
            @DecimalMin(value = "0.01", message = "Principal amount must be greater than zero")
            BigDecimal principalAmount,

            @NotNull(message = "Date received is required")
            LocalDate dateReceived,

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
    public record RepayLoanRequest(
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
    public record UpdateLoanRequest(
            LocalDate dueDate,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record LoanAccountResponse(
            Long id,
            Long investorId,
            String investorName,
            Long branchId,
            String branchName,
            BigDecimal principalAmount,
            BigDecimal outstandingBalance,
            LoanStatus status,
            LocalDate dateReceived,
            LocalDate dueDate,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record LoanTransactionResponse(
            Long id,
            Long loanAccountId,
            Long branchId,
            String branchName,
            LoanTransactionType transactionType,
            BigDecimal amount,
            LocalDate transactionDate,
            String reference,
            String description,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
