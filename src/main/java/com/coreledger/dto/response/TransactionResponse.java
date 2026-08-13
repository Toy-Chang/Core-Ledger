package com.coreledger.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String transactionNo,
        Long accountId,
        String transactionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String requestId,
        String remark,
        LocalDateTime createdAt
) {
}
