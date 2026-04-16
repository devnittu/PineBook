package com.pinebook.api.util;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Logs every inbound HTTP request with method, path, status, and duration.
 *
 * Log format (INFO):
 *   [correlationId] METHOD /path → STATUS (Xms)
 *
 * Runs after CorrelationIdFilter (Order=2) so MDC.correlationId is already set.
 */
@Component
@Order(2)
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long start = Instant.now().toEpochMilli();

        try {
            chain.doFilter(request, response);
        } finally {
            long durationMs = Instant.now().toEpochMilli() - start;
            log.info("{} {} → {} ({}ms)",
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    durationMs);
        }
    }
}
