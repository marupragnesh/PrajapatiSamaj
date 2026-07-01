package com.matrimonial.service;

import com.matrimonial.dto.request.*;
import com.matrimonial.dto.response.AuthResponse;
import com.matrimonial.entity.User;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.repository.UserRepository;
import com.matrimonial.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SERVICE: AuthService
 *
 * Handles all authentication business logic:
 *   - Register a new user
 *   - Login and return JWT
 *   - Forgot Password (OTP flow delegated to OtpService)
 *   - Reset Password
 *
 * Email normalization: all emails are stored in lowercase regardless of user input.
 *
 * Layer: Service (all business logic lives here — no DB queries, no HTTP logic)
 */
@Service
@RequiredArgsConstructor
@Slf4j
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
     *   - Email is normalized to lowercase before saving
     *   - Email must not already be registered
     *   - Password is BCrypt-hashed before saving
     *   - Returns JWT immediately (user is auto-logged in after register)
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Normalize email — always lowercase regardless of how user typed it
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("Email is already registered. Please login.");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        log.info("User registered successfully — email={}", savedUser.getEmail());

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
     *   - Email is normalized to lowercase before lookup
     *   - Spring Security verifies credentials via AuthenticationManager
     *   - If valid, generate and return JWT
     */
    public AuthResponse login(LoginRequest request) {

        // Normalize email before authentication
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.info("Login failed — email={}, reason={}", normalizedEmail, e.getMessage());
            throw e;
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.info("Login failed — email={}, reason=User not found", normalizedEmail);
                    return new ResourceNotFoundException("User not found");
                });

        if (!user.getIsActive()) {
            log.info("Login failed — email={}, reason=Account deactivated", normalizedEmail);
            throw new BadRequestException("Your account has been deactivated. Please contact support.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        log.info("Login successful — email={}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .message("Login successful!")
                .build();
    }

    /**
     * Step 1 of Forgot Password: Send OTP to email.
     * Email normalized before lookup.
     */
    public void forgotPassword(ForgotPasswordRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        // Don't reveal whether email exists (security best practice)
        if (!userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        otpService.generateAndSendOtp(normalizedEmail);
    }

    /**
     * Step 2 of Forgot Password: Verify OTP.
     */
    public void verifyOtp(VerifyOtpRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        otpService.verifyOtp(normalizedEmail, request.getOtpCode());
    }

    /**
     * Step 3 of Forgot Password: Reset password after OTP verified.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        otpService.verifyOtp(normalizedEmail, request.getOtpCode());

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpService.deleteOtpsByEmail(normalizedEmail);
    }

    /** Helper: get User entity by email (used by other services) */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
