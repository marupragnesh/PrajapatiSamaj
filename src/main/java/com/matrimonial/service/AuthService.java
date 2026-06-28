package com.matrimonial.service;

import com.matrimonial.dto.request.*;
import com.matrimonial.dto.response.AuthResponse;
import com.matrimonial.entity.User;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.repository.UserRepository;
import com.matrimonial.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SERVICE: AuthService
 *
 * Handles all authentication business logic:
 *   - Register a new user
 *   - Login and return JWT
 *   - Forgot Password (OTP flow delegated to OtpService)
 *   - Reset Password
 *
 * Layer: Service (all business logic lives here — no DB queries, no HTTP logic)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    /**
     * Register a new user with email + password.
     *
     * Business rules:
     *   - Email must not already be registered
     *   - Password is BCrypt-hashed before saving
     *   - Returns JWT immediately (user is auto-logged in after register)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check if email is already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered. Please login.");
        }

        // Build the user entity with hashed password
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword())) // BCrypt hash
                .isActive(true)
                .build();

        // Save user to DB
        User savedUser = userRepository.save(user);

        // Generate JWT token for auto-login after registration
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .message("Registration successful! Please complete your profile.")
                .build();
    }


    /**
     * Login with email + password.
     *
     * Business rules:
     *   - Spring Security verifies credentials via AuthenticationManager
     *   - If valid, generate and return JWT
     *   - If invalid, Spring Security throws an exception automatically
     */
    public AuthResponse login(LoginRequest request) {

        // Delegate credential verification to Spring Security
        // This will throw BadCredentialsException if email/password is wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Credentials verified — load the user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if account is active (not deactivated by admin)
        if (!user.getIsActive()) {
            throw new BadRequestException("Your account has been deactivated. Please contact support.");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .message("Login successful!")
                .build();
    }

    /**
     * Step 1 of Forgot Password: Send OTP to email.
     * Delegates to OtpService.
     */
    public void forgotPassword(ForgotPasswordRequest request) {

        // Verify user exists with this email
        if (!userRepository.existsByEmail(request.getEmail())) {
            // Don't reveal whether email exists (security best practice)
            // Just return silently — frontend shows "if registered, OTP sent"
            return;
        }

        // Generate and send OTP
        otpService.generateAndSendOtp(request.getEmail());
    }

    /**
     * Step 2 of Forgot Password: Verify OTP.
     * Delegates to OtpService.
     */
    public void verifyOtp(VerifyOtpRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtpCode());
    }

    /**
     * Step 3 of Forgot Password: Reset password after OTP verified.
     *
     * Business rules:
     *   - Re-verify OTP again for security
     *   - BCrypt-hash the new password before saving
     *   - Clean up OTP records after reset
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        // Re-verify OTP to confirm it's still valid
        otpService.verifyOtp(request.getEmail(), request.getOtpCode());

        // Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Hash new password and save
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Delete all OTPs for this email (cleanup)
        otpService.deleteOtpsByEmail(request.getEmail());
    }

    /**
     * Helper: Get currently logged-in User entity from email.
     * Called by other services to get the full User object.
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
