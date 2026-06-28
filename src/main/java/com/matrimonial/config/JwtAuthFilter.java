package com.matrimonial.config;

import com.matrimonial.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CONFIG: JwtAuthFilter
 *
 * Intercepts every HTTP request ONCE and checks for a valid JWT token.
 *
 * Flow:
 *   1. Read the "Authorization" header
 *   2. Extract the token (format: "Bearer <token>")
 *   3. Validate the token using JwtUtil
 *   4. Load user from DB using email inside the token
 *   5. Set the user as "authenticated" in Spring Security context
 *   6. Pass request along to the next filter/controller
 *
 * If token is missing or invalid → request proceeds unauthenticated
 * (protected endpoints will then return 401 automatically).
 *
 * Layer: Config (security infrastructure — not business logic)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Get the Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No token found — skip JWT auth, let Spring Security handle it
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract token by removing "Bearer " prefix (7 characters)
        String token = authHeader.substring(7);

        // Step 4: Validate token and extract email
        if (!jwtUtil.isTokenValid(token)) {
            // Invalid or expired token — don't set authentication
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtUtil.extractEmail(token);

        // Step 5: Only set authentication if user not already authenticated
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user from DB via email
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Create authentication object with user details
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,                       // credentials (not needed after auth)
                            userDetails.getAuthorities() // roles/permissions
                    );

            // Attach request details (IP, session) to the auth token
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Step 6: Set user as authenticated in Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Continue to the next filter or the controller
        filterChain.doFilter(request, response);
    }
}
