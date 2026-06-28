package com.matrimonial.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * UTIL: OtpUtil
 *
 * Generates a 6-digit numeric OTP (One-Time Password).
 * Used in the Forgot Password flow.
 *
 * Uses SecureRandom instead of Random for cryptographic safety.
 *
 * Layer: Util (helper class, used by OtpService)
 */
@Component
public class OtpUtil {

    // SecureRandom is cryptographically safe (better than Math.random())
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a random 6-digit OTP.
     * Range: 100000 to 999999 (always exactly 6 digits)
     *
     * @return 6-digit OTP as String
     */
    public String generateOtp() {
        // nextInt(900000) gives 0–899999, adding 100000 ensures 6 digits
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }
}
