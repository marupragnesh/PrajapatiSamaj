package com.matrimonial.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO: RegisterRequest
 *
 * Incoming data when a new user registers.
 * Validated before reaching the service layer.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class RegisterRequest {

    // Must be a valid email format
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    // Password must be at least 6 characters
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
