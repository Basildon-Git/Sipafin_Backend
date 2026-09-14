package com.basiltech.sipafin.dto;

import java.time.Instant;

public record ApiResponse<T>(
        int status,
        String message,
        T data,
        Instant timestamp
) {
    public static <T> ApiResponse<T> of(int status, String message, T data) {
        return new ApiResponse<>(status, message, data, Instant.now());
    }
}
