package com.mauripay.backend.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Spring Security's 401/403 happen in the filter chain, before {@code @RestControllerAdvice},
 * so they bypass {@link GlobalExceptionHandler}. These write the same JSON
 * {@link GlobalExceptionHandler.ErrorResponse} body (with a stable {@code code}) instead of
 * an empty/HTML error page, so clients always get a coded error to translate.
 */
public final class SecurityErrorHandlers {

    private SecurityErrorHandlers() {
    }

    @Component
    public static class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
        private final ObjectMapper mapper;

        public JsonAuthenticationEntryPoint(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException ex) throws IOException {
            write(mapper, response, HttpStatus.UNAUTHORIZED, ErrorCode.NOT_AUTHENTICATED,
                    "Authentication required.", request.getRequestURI());
        }
    }

    @Component
    public static class JsonAccessDeniedHandler implements AccessDeniedHandler {
        private final ObjectMapper mapper;

        public JsonAccessDeniedHandler(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           AccessDeniedException ex) throws IOException {
            write(mapper, response, HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED,
                    "You don't have access to this resource.", request.getRequestURI());
        }
    }

    private static void write(ObjectMapper mapper, HttpServletResponse response, HttpStatus status,
                              String code, String message, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getWriter(),
                new GlobalExceptionHandler.ErrorResponse(status, code, message, path, Map.of()));
    }
}
