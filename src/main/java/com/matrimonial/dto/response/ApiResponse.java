package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO: ApiResponse
 *
 * A generic wrapper for simple success/error messages.
 * Used when we only need to return a status message (not full data).
 *
 * Example usages:
 *   - "OTP sent successfully"
 *   - "Password reset successfully"
 *   - "Profile updated"
 *   - "Like sent"
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    // true = success, false = failure
    private boolean success;

    // Human-readable message for the frontend
    private String message;

    // Optional: timestamp of the response
    private LocalDateTime timestamp;

    // ===== Convenience factory methods =====

    // Quickly create a success response
    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Quickly create an error response
    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
