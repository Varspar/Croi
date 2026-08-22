package com.croi.common.filter;

import com.croi.common.dto.ApiError;
import com.croi.common.exception.ErrorCode;
import com.croi.common.util.RateLimiter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Limits POST /api/v1/contact to 5 submissions/hour per client IP. Runs before
 * JwtFilter since this endpoint is unauthenticated — there's no principal to key
 * the limit on, only the request's origin IP.
 */
@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RATE_LIMITED_PATH = "/api/v1/contact";

    private final RateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean isRateLimited = "POST".equalsIgnoreCase(request.getMethod())
                && RATE_LIMITED_PATH.equals(request.getRequestURI());

        if (isRateLimited && !rateLimiter.allowRequest(resolveClientIp(request))) {
            writeTooManyRequests(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // The header can be a comma-separated chain (client, proxy1, proxy2, ...);
            // the first entry is the original client.
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeTooManyRequests(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApiError error = ApiError.builder()
                .errorCode(ErrorCode.RATE_LIMIT_EXCEEDED.name())
                .message("Too many requests. Please try again in 1 hour.")
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
