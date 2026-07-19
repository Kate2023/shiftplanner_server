package com.example.shiftplanner_server;

import com.example.shiftplanner_server.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String API_PREFIX = "/api/";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String BEARER_PREFIX = "Bearer ";
    private final TokenService tokenService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = getPathWithinApplication(request);
        return !path.startsWith(API_PREFIX) || LOGIN_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (token == null) {
            writeUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        if (tokenService.getByToken(token).isEmpty()) {
            writeUnauthorized(response, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"reason\":\"" + message + "\"}");
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        String value = stripWrappingQuotes(authorizationHeader.trim());
        if (value.isEmpty()) {
            return null;
        }

        if (value.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            value = value.substring(BEARER_PREFIX.length()).trim();
        }

        // Also accept raw UUID token values without the Bearer prefix.
        String token = stripWrappingQuotes(value);
        return token.isEmpty() ? null : token;
    }

    private String stripWrappingQuotes(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
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

