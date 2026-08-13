package com.coreledger.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNo,
        Long userId,
        String accountName,
        BigDecimal balance,
        String status,
        Integer version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
