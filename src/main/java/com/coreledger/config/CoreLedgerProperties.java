package com.coreledger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "core-ledger")
public record CoreLedgerProperties(Jwt jwt, Idempotency idempotency, Cache cache) {
    public record Jwt(String secret, long expirationSeconds) {
    }

    public record Idempotency(long ttlSeconds) {
    }

    public record Cache(long accountTtlSeconds, long nullTtlSeconds) {
    }
}
