package com.coreledger.common.api;

import com.coreledger.common.util.RequestIdHolder;
import java.time.Instant;

public record ApiResponse<T>(
        int code,
        String message,
        T data,
        Instant timestamp,
        String requestId
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, Instant.now(), RequestIdHolder.get());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, Instant.now(), RequestIdHolder.get());
    }
}
