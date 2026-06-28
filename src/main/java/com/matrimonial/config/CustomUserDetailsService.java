package com.matrimonial.config;

import com.matrimonial.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * CONFIG: CustomUserDetailsService
 *
 * Loads a user from the DB by email for Spring Security.
 * Extracted into its own @Component to break the circular dependency:
 *
 *   BEFORE (cycle):
 *     JwtAuthFilter → UserDetailsService (bean in SecurityConfig)
 *     SecurityConfig → JwtAuthFilter
 *
 *   AFTER (no cycle):
 *     JwtAuthFilter → CustomUserDetailsService (independent @Component)
 *     SecurityConfig → CustomUserDetailsService (independent @Component)
 *
 * Layer: Config (security infrastructure)
 */
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by email — called by Spring Security during authentication
     * and by JwtAuthFilter on every protected request.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }
}
