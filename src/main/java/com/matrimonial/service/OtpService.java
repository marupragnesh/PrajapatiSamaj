package com.matrimonial.service;

import com.matrimonial.entity.OtpToken;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.repository.OtpRepository;
import com.matrimonial.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * SERVICE: OtpService
 *
 * Handles OTP generation, storage, and verification.
 *
 * Business rules:
 *   - OTP is 6 digits
 *   - Expires in 10 minutes (configurable via application.properties)
 *   - Single use only — marked as used after first verification
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpUtil otpUtil;
    private final EmailService emailService;

    // OTP expiry in minutes — read from application.properties
    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    /**
     * Generate a new OTP, save it to DB, and send it via email.
     *
     * @param email the user's email to send OTP to
     */
    @Transactional
    public void generateAndSendOtp(String email) {

        // Generate random 6-digit OTP
        String otp = otpUtil.generateOtp();

        // Set expiry time (now + 10 minutes)
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        // Save OTP to DB
        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otp)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        otpRepository.save(otpToken);

        // Send OTP via email
        emailService.sendOtpEmail(email, otp);
    }

    /**
     * Verify the OTP entered by the user.
     *
     * Checks:
     *   1. OTP exists for this email and code
     *   2. OTP has not been used yet
     *   3. OTP has not expired
     *
     * If all checks pass, marks OTP as used.
     *
     * @param email   user's email
     * @param otpCode OTP entered by user
     */
    @Transactional
    public void verifyOtp(String email, String otpCode) {

        // Find matching unused OTP
        OtpToken otpToken = otpRepository
                .findByEmailAndIsUsedFalseAndOtpCode(email, otpCode)
                .orElseThrow(() -> new BadRequestException("Invalid OTP. Please check and try again."));

        // Check if OTP has expired
        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        // Mark OTP as used (single-use enforcement)
        otpToken.setIsUsed(true);
        otpRepository.save(otpToken);
    }

    /**
     * Delete all OTPs for an email (cleanup after password reset).
     *
     * @param email user's email
     */
    @Transactional
    public void deleteOtpsByEmail(String email) {
        otpRepository.deleteByEmail(email);
    }
}
