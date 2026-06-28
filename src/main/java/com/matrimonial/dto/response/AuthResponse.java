package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: AuthResponse
 *
 * Returned after successful registration or login.
 * Contains the JWT token the frontend will use for all future requests.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // JWT token — frontend stores this and sends it in Authorization header
    private String token;

    // User's ID — useful for frontend to store and reference
    private Long userId;

    // User's email — for display purposes
    private String email;

    // Simple message like "Login successful" or "Registration successful"
    private String message;
}
