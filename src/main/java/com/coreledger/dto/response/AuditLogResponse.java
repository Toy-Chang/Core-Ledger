package com.coreledger.dto.response;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long userId,
        String username,
        String operation,
        String resource,
        String requestMethod,
        String requestUri,
        String requestIp,
        String requestId,
        String result,
        LocalDateTime createdAt
) {
}
