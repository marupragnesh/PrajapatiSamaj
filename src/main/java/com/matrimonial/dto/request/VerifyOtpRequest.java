package com.matrimonial.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO: VerifyOtpRequest
 *
 * Used when user submits the OTP received in their email.
 * Service verifies OTP is valid, not expired, and not already used.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    // 6-digit OTP entered by user
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String otpCode;
}
