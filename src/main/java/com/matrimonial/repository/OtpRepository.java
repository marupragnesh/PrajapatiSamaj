package com.matrimonial.repository;

import com.matrimonial.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY: OtpRepository
 *
 * Handles all DB operations for the "otp_tokens" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface OtpRepository extends JpaRepository<OtpToken, Long> {

    /**
     * Find the latest unused, unexpired OTP for a given email.
     * Used during OTP verification step.
     *
     * Service layer will check:
     *   - is_used = false
     *   - expires_at > now()
     */
    Optional<OtpToken> findByEmailAndIsUsedFalseAndOtpCode(String email, String otpCode);

    // Delete all OTPs for an email (cleanup after successful password reset)
    void deleteByEmail(String email);
}
