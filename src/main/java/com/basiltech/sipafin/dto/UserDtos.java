package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UserDtos {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LoginRequest(
            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Password is required")
            String password
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AuthResponse(
            String tokenType,
            String accessToken,
            Long expiresIn,
            UserResponse user
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RegisterUserRequest(
            @NotBlank(message = "Full name is required")
            String fullName,

            @NotBlank(message = "Username is required")
            String username,

            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters")
            String password,

            @NotNull(message = "Role is required")
            UserRole role,

            Long branchId
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserResponse(
            Long id,
            String fullName,
            String username,
            UserRole role,
            Long branchId,
            String branchName,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UpdateUserRequest(
            @NotBlank(message = "Full name is required")
            String fullName,

            @NotNull(message = "Role is required")
            UserRole role,

            Long branchId,

            boolean enabled
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChangePasswordRequest(
            @NotBlank(message = "Current password is required")
            String currentPassword,

            @NotBlank(message = "New password is required")
            @Size(min = 6, message = "New password must be at least 6 characters")
            String newPassword
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResetPasswordRequest(
            @NotBlank(message = "New password is required")
            @Size(min = 6, message = "New password must be at least 6 characters")
            String newPassword
    ) {
    }
}