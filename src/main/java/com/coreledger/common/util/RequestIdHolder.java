package com.coreledger.common.util;

import org.slf4j.MDC;

public final class RequestIdHolder {
    public static final String HEADER = "X-Request-Id";
    public static final String MDC_KEY = "requestId";

    private RequestIdHolder() {
    }

    public static String get() {
        return MDC.get(MDC_KEY);
    }
}
