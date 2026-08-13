package com.coreledger.dto.response;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
