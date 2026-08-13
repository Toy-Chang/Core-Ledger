package com.coreledger.common.filter;

import com.coreledger.common.util.RequestIdHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(RequestIdHolder.HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(RequestIdHolder.MDC_KEY, requestId);
        response.setHeader(RequestIdHolder.HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(RequestIdHolder.MDC_KEY);
        }
    }
}
