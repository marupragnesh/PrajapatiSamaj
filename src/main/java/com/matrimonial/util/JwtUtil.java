package com.matrimonial.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * UTIL: JwtUtil
 *
 * Handles all JWT (JSON Web Token) operations:
 *   - Generate token after login
 *   - Extract email (subject) from token
 *   - Validate token (not expired, correct signature)
 *
 * Token format: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 * Layer: Util (helper class, used by SecurityConfig and AuthService)
 */
@Component
@Slf4j
public class JwtUtil {

    // Secret key from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    // Token expiry in milliseconds (default: 86400000 = 24 hours)
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ===== Generate signing key from the secret string =====
    private Key getSigningKey() {
        // Convert our secret string to a secure HMAC-SHA key
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generate a JWT token for the given email.
     * Called after successful login or registration.
     *
     * @param email the user's email (stored as JWT "subject")
     * @return signed JWT string
     */
    public String generateToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(email)           // who this token belongs to
                .setIssuedAt(now)            // when was it created
                .setExpiration(expiry)       // when does it expire
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // sign with our secret
                .compact();
    }

    /**
     * Extract the email (subject) from a JWT token.
     * Used by JwtAuthFilter to identify the logged-in user.
     *
     * @param token the JWT string
     * @return email stored inside the token
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Check if the token is still valid (not expired and correctly signed).
     *
     * @param token the JWT string
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            // Token is valid if expiry date is after right now
            return !claims.getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT expired — {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("JWT signature verification failed — {}", e.getMessage());
            return false;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("JWT malformed — {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("JWT validation failed — {}", e.getMessage());
            return false;
        }
    }

    // ===== Parse and return all claims from the token =====
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
