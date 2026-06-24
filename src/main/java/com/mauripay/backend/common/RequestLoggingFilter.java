package com.mauripay.backend.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Logs one line per request with a correlation id, timing, status and the resolved principal.
 * Ordered after Spring Security's filter chain (which sits at -100) so the SecurityContext is
 * still populated when we read the principal.
 */
@Component
@Order(0)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("http.access");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            long tookMs = System.currentTimeMillis() - start;
            log.info("{} {} -> {} ({} ms) principal={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    tookMs,
                    currentPrincipal());
            MDC.remove("requestId");
        }
    }

    private String currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth == null || !auth.isAuthenticated()) ? "anonymous" : auth.getName();
    }
}
