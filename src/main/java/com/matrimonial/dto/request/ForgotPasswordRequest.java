package com.matrimonial.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO: ForgotPasswordRequest
 *
 * Used when user requests OTP to reset their password.
 * Only the email is needed.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;
}
