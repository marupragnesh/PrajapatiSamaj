package com.matrimonial.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * FILTER: RequestLoggingFilter
 *
 * Automatically logs every incoming HTTP request method, URI, response status,
 * and duration (ms).
 *
 * Logger category: "RequestLogger"
 * Format: → GET /api/profile/me — 200 OK — 45ms
 *
 * Layer: Config (cross-cutting logging filter)
 */
@Component
@Slf4j(topic = "RequestLogger")
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();

            String statusText;
            try {
                HttpStatus httpStatus = HttpStatus.resolve(status);
                statusText = (httpStatus != null) ? status + " " + httpStatus.name() : String.valueOf(status);
            } catch (Exception e) {
                statusText = String.valueOf(status);
            }

            log.info("→ {} {} — {} — {}ms", method, uri, statusText, duration);
        }
    }
}
