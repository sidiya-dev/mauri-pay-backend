package com.mauripay.backend.merchant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authenticates merchant requests carrying the {@code X-Api-Key} header.
 * If the header is absent the filter is a no-op, letting session auth or the
 * security rules handle the request.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Api-Key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER);
        if (apiKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            apiKeyService.authenticate(apiKey).ifPresent(merchant ->
                    SecurityContextHolder.getContext()
                            .setAuthentication(new MerchantAuthentication(merchant.getId())));
        }
        filterChain.doFilter(request, response);
    }
}
