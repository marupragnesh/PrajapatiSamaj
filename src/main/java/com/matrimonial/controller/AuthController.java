package com.matrimonial.controller;

import com.matrimonial.dto.request.*;
import com.matrimonial.dto.response.ApiResponse;
import com.matrimonial.dto.response.AuthResponse;
import com.matrimonial.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: AuthController
 *
 * Handles all authentication HTTP endpoints:
 *   POST /api/auth/register         - Register new user
 *   POST /api/auth/login            - Login and get JWT
 *   POST /api/auth/forgot-password  - Send OTP to email
 *   POST /api/auth/verify-otp       - Verify OTP code
 *   POST /api/auth/reset-password   - Set new password
 *
 * IMPORTANT: No business logic here.
 * Controller only receives request → calls service → returns response.
 *
 * Layer: Controller (HTTP in/out only)
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user with email + password.
     * Returns JWT on success (user is auto-logged in).
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login with email + password.
     * Returns JWT on success.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 1: Forgot Password — send OTP to email.
     * Returns generic success message (doesn't reveal if email exists).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
            ApiResponse.success("If this email is registered, an OTP has been sent.")
        );
    }

    /**
     * Step 2: Verify the OTP entered by the user.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully. You may now reset your password."));
    }

    /**
     * Step 3: Reset password after OTP is verified.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. Please login with your new password."));
    }
}
