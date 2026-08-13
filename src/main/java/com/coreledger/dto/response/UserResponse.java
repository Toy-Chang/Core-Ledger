package com.coreledger.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        String status,
        List<String> roles,
        LocalDateTime createdAt
) {
}
