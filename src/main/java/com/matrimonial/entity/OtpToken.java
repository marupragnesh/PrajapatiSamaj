package com.matrimonial.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: OtpToken
 *
 * Maps to the "otp_tokens" table.
 * Stores OTP codes sent to users for "Forgot Password" flow.
 *
 * Business rules (enforced in service layer):
 *   - OTP is 6 digits
 *   - Expires in 10 minutes
 *   - Single-use only (is_used = true after first use)
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "otp_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email address to which OTP was sent
    @Column(nullable = false, length = 100)
    private String email;

    // The 6-digit OTP code
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    // OTP becomes invalid after this time (10 minutes from creation)
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Once OTP is verified, mark it as used so it can't be reused
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
