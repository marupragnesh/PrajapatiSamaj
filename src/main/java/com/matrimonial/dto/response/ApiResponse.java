package com.matrimonial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO: ApiResponse
 *
 * A generic wrapper for success/error messages and payload data responses.
 *
 * Example usages:
 *   - "OTP sent successfully" -> ApiResponse.success("OTP sent successfully")
 *   - "Profile updated" -> ApiResponse.success("Profile updated", data)
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    // true = success, false = failure
    private boolean success;

    // Human-readable message for the frontend
    private String message;

    // Optional payload data
    private T data;

    // Optional: timestamp of the response
    private LocalDateTime timestamp;

    // ===== Convenience factory methods =====

    // Quickly create a success response without data
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Quickly create a success response with payload data
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Quickly create an error response
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
