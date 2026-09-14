package com.basiltech.sipafin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class BankAccountDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateBankAccountRequest(
            @NotNull(message = "Bank ID is required")
            Long bankId,

            Long branchId,

            @NotBlank(message = "Account name is required")
            @Size(max = 150, message = "Account name must not exceed 150 characters")
            String accountName,

            @NotBlank(message = "Account number is required")
            @Size(max = 100, message = "Account number must not exceed 100 characters")
            String accountNumber,

            @NotBlank(message = "Currency is required")
            @Size(max = 10, message = "Currency must not exceed 10 characters")
            String currency,

            @NotNull(message = "Opening balance is required")
            @DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
            BigDecimal openingBalance,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateBankAccountRequest(
            @NotNull(message = "Bank ID is required")
            Long bankId,

            Long branchId,

            @NotBlank(message = "Account name is required")
            @Size(max = 150, message = "Account name must not exceed 150 characters")
            String accountName,

            @NotBlank(message = "Account number is required")
            @Size(max = 100, message = "Account number must not exceed 100 characters")
            String accountNumber,

            @NotBlank(message = "Currency is required")
            @Size(max = 10, message = "Currency must not exceed 10 characters")
            String currency,

            boolean active,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankAccountStatusRequest(
            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record BankAccountResponse(
            Long id,
            Long bankId,
            String bankName,
            String bankCode,
            Long branchId,
            String branchName,
            String accountName,
            String accountNumber,
            String currency,
            BigDecimal currentBalance,
            boolean active,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
