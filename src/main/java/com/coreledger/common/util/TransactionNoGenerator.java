package com.coreledger.common.util;

import java.security.SecureRandom;
import java.time.Instant;

public final class TransactionNoGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private TransactionNoGenerator() {
    }

    public static String next() {
        return "TX" + Instant.now().toEpochMilli() + String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
