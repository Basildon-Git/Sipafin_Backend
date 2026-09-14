package com.basiltech.sipafin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class InvestorDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreateInvestorRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 150, message = "Full name must not exceed 150 characters")
            String fullName,

            @NotBlank(message = "Phone number is required")
            @Size(max = 50, message = "Phone number must not exceed 50 characters")
            String phoneNumber,

            @Size(max = 100, message = "National ID must not exceed 100 characters")
            String nationalId,

            @Size(max = 255, message = "Address must not exceed 255 characters")
            String address,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateInvestorRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 150, message = "Full name must not exceed 150 characters")
            String fullName,

            @NotBlank(message = "Phone number is required")
            @Size(max = 50, message = "Phone number must not exceed 50 characters")
            String phoneNumber,

            @Size(max = 100, message = "National ID must not exceed 100 characters")
            String nationalId,

            @Size(max = 255, message = "Address must not exceed 255 characters")
            String address,

            boolean active,

            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InvestorStatusRequest(
            @NotBlank(message = "Actioned by is required")
            String actionedBy
    ) {
    }

    public record InvestorResponse(
            Long id,
            String fullName,
            String phoneNumber,
            String nationalId,
            String address,
            boolean active,
            String actionedBy,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
