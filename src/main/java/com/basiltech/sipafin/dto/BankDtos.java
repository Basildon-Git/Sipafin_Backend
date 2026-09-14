package com.basiltech.sipafin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class BankDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateBankRequest(
            @NotBlank(message = "Bank name is required")
            @Size(max = 150, message = "Bank name must not exceed 150 characters")
            String name,

            @NotBlank(message = "Bank code is required")
            @Size(max = 50, message = "Bank code must not exceed 50 characters")
            String code,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateBankRequest(
            @NotBlank(message = "Bank name is required")
            @Size(max = 150, message = "Bank name must not exceed 150 characters")
            String name,

            @NotBlank(message = "Bank code is required")
            @Size(max = 50, message = "Bank code must not exceed 50 characters")
            String code,

            boolean active,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BankStatusRequest(
            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record BankResponse(
            Long id,
            String name,
            String code,
            boolean active,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
