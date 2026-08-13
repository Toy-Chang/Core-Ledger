package com.coreledger.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(max = 64) String requestId,
        @Size(max = 255) String remark
) {
}
