package com.coreledger.common.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class AccountNumberGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    private AccountNumberGenerator() {
    }

    public static String next() {
        int suffix = RANDOM.nextInt(1_000_000);
        return "CL" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%06d", suffix);
    }
}
