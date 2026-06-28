package com.matrimonial.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO: ResetPasswordRequest
 *
 * Used in the final step of Forgot Password flow.
 * User submits their email, verified OTP, and new password.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    // OTP must be re-submitted to confirm identity
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String otpCode;

    // New password the user wants to set
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}
