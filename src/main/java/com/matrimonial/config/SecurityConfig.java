package com.matrimonial.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * CONFIG: SecurityConfig
 * <p>
 * Configures Spring Security for the entire application:
 * - Which endpoints are public vs protected
 * - JWT stateless session (no cookies, no server-side session)
 * - BCrypt password encoding
 * - Custom JWT filter runs before the default auth filter
 * <p>
 * Circular dependency fix:
 * UserDetailsService is now a standalone @Component (CustomUserDetailsService).
 * SecurityConfig no longer defines it as a @Bean, so JwtAuthFilter
 * can inject it without depending on SecurityConfig.
 * <p>
 * Layer: Config (security setup — not business logic)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // No longer injects JwtAuthFilter — received as method param in securityFilterChain()
    private final CustomUserDetailsService userDetailsService;

    /**
     * AuthenticationProvider — ties CustomUserDetailsService + PasswordEncoder together.
     * Spring Security uses this to verify login credentials.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager — used by AuthService to trigger authentication.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS config — allows React frontend (Vite) to call the backend.
     * Without this, browser blocks all requests from http://localhost:5173.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    /**
     * BCryptPasswordEncoder — hashes passwords with strength 12.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * SecurityFilterChain — the core security rules.
     * <p>
     * JwtAuthFilter is injected as a method parameter here instead of
     * a class field. This avoids SecurityConfig → JwtAuthFilter dependency
     * at bean construction time, which was causing the cycle.
     * <p>
     * Public endpoints (no JWT needed):
     * POST /api/auth/register
     * POST /api/auth/login
     * POST /api/auth/forgot-password
     * POST /api/auth/verify-otp
     * POST /api/auth/reset-password
     * <p>
     * All other endpoints require a valid JWT token.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                // Disable CSRF — not needed for stateless JWT REST APIs
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // ← ADD THIS LINE


                // Define which endpoints are public vs protected
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .anyRequest().authenticated()
                )

                // Stateless session — no server-side sessions, JWT handles state
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Use our custom authentication provider
                .authenticationProvider(authenticationProvider())

                // Run JWT filter before Spring's default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
