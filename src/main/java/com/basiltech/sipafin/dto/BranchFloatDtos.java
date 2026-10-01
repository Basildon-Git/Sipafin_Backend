package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class BranchFloatDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateBranchFloatAccountRequest(
            @NotNull(message = "Branch ID is required")
            Long branchId,

            @NotNull(message = "Currency is required")
            CurrencyCode currency,

            @NotNull(message = "Opening balance is required")
            @DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
            BigDecimal openingBalance,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BranchFloatStatusRequest(
            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record BranchFloatAccountResponse(
            Long id,
            Long branchId,
            String branchName,
            String branchCode,
            CurrencyCode currency,
            BigDecimal currentBalance,
            boolean active,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}