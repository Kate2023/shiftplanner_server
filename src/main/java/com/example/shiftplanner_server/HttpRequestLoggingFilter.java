package com.example.shiftplanner_server;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);
    private static final String API_PREFIX = "/api/";

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = getPathWithinApplication(request);
        return !path.startsWith(API_PREFIX);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;
            String query = request.getQueryString();
            String pathAndQuery = query == null || query.isBlank()
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + query;

            log.info("HTTP {} {} -> {} ({} ms)",
                request.getMethod(),
                pathAndQuery,
                response.getStatus(),
                elapsedMillis);
        }
    }

    private String getPathWithinApplication(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String uri = request.getRequestURI();

        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            String normalized = uri.substring(contextPath.length());
            return normalized.isEmpty() ? "/" : normalized;
        }

        return request.getServletPath();
    }
}

