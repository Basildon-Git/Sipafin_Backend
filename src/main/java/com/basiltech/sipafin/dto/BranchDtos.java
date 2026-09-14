package com.basiltech.sipafin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public class BranchDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateBranchRequest(
            @NotBlank(message = "Branch name is required")
            @Size(max = 150, message = "Branch name must not exceed 150 characters")
            String name,

            @NotBlank(message = "Branch code is required")
            @Size(max = 50, message = "Branch code must not exceed 50 characters")
            String code,

            @Size(max = 255, message = "Location must not exceed 255 characters")
            String location,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateBranchRequest(
            @NotBlank(message = "Branch name is required")
            @Size(max = 150, message = "Branch name must not exceed 150 characters")
            String name,

            @NotBlank(message = "Branch code is required")
            @Size(max = 50, message = "Branch code must not exceed 50 characters")
            String code,

            @Size(max = 255, message = "Location must not exceed 255 characters")
            String location,

            boolean active,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BranchStatusRequest(
            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record BranchResponse(
            Long id,
            String name,
            String code,
            String location,
            BigDecimal currentCashFloatBalance,
            boolean active,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}