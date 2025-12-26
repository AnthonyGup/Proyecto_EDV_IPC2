package com.backend.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class CorsFilter extends HttpFilter {

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:4200"
    );

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String origin = request.getHeader("Origin");
        if (origin != null && (ALLOWED_ORIGINS.contains(origin))) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
        }

        response.setHeader("Access-Control-Allow-Credentials", "true");

        String reqMethod = request.getHeader("Access-Control-Request-Method");
        if (reqMethod == null || reqMethod.isBlank()) {
            reqMethod = "GET, POST, PUT, DELETE, OPTIONS";
        }
        response.setHeader("Access-Control-Allow-Methods", reqMethod);

        String reqHeaders = request.getHeader("Access-Control-Request-Headers");
        if (reqHeaders == null || reqHeaders.isBlank()) {
            reqHeaders = "Origin, Content-Type, Accept, Authorization";
        }
        response.setHeader("Access-Control-Allow-Headers", reqHeaders);
        response.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(request, response);
    }
}
