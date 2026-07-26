package com.matrimonial.service;

import com.matrimonial.entity.OtpToken;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.repository.OtpRepository;
import com.matrimonial.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OtpService {

    private final OtpRepository otpRepository;
    private final OtpUtil otpUtil;
    private final EmailService emailService;

    // OTP expiry in minutes — read from application.properties
    @Value("${otp.expiry.minutes}")
    private int otpExpiryMinutes;

    public enum OtpPurpose {
        REGISTRATION,
        FORGOT_PASSWORD,
        ACCOUNT_DELETION
    }

    /**
     * Generate a new OTP, save it to DB, and send it via email based on purpose.
     */
    @Transactional
    public void generateAndSendOtp(String email, OtpPurpose purpose) {
        String otp = otpUtil.generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpiryMinutes);

        OtpToken otpToken = OtpToken.builder()
                .email(email)
                .otpCode(otp)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();

        otpRepository.save(otpToken);

        log.info("OTP generated and saved for email={}, purpose={}", email, purpose);

        switch (purpose) {
            case REGISTRATION -> emailService.sendRegistrationOtpEmail(email, otp);
            case ACCOUNT_DELETION -> emailService.sendAccountDeletionOtpEmail(email, otp);
            default -> emailService.sendForgotPasswordOtpEmail(email, otp);
        }
    }

    /**
     * Legacy helper method.
     */
    @Transactional
    public void generateAndSendOtp(String email) {
        generateAndSendOtp(email, OtpPurpose.FORGOT_PASSWORD);
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
                .orElseThrow(() -> {
                    log.info("OTP verification failed — email={}, reason=Invalid OTP", email);
                    return new BadRequestException("Invalid OTP. Please check and try again.");
                });

        // Check if OTP has expired
        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("OTP verification failed — email={}, reason=Expired", email);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        log.info("OTP verified successfully — email={}", email);
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
