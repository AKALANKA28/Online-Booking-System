package com.ticketing.paymentnotificationservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String expectedInternalApiKey;

    public InternalApiKeyFilter(@Value("${app.security.internal-api-key}") String expectedInternalApiKey) {
        this.expectedInternalApiKey = expectedInternalApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/internal/")) {
            String actualKey = request.getHeader("X-Internal-Token");
            if (!expectedInternalApiKey.equals(actualKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Invalid internal API key\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
