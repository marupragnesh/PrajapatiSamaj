package com.matrimonial.entity;

import com.matrimonial.entity.enums.PaymentFeature;
import com.matrimonial.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: Payment
 *
 * Maps to the "payments" table. One row per Razorpay order attempt.
 * A row starts as CREATED (order placed), then becomes SUCCESS or FAILED
 * once /api/payments/verify checks the Razorpay signature.
 *
 * `feature` records WHAT the payment is for (see PaymentFeature). This is
 * the design choice that lets new locked features reuse the same table and
 * the same PaymentService.hasUnlocked(user, feature) check, instead of a
 * new payment table per feature.
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Who made this payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // WHAT this payment unlocks — CONTACT_UNLOCK today, more values later
    @Enumerated(EnumType.STRING)
    @Column(name = "feature", nullable = false, length = 30)
    private PaymentFeature feature;

    @Column(name = "razorpay_order_id", nullable = false, length = 50)
    private String razorpayOrderId;

    // Null until payment is completed and verified
    @Column(name = "razorpay_payment_id", length = 50)
    private String razorpayPaymentId;

    // Amount in paise (smallest currency unit), matches what was sent to Razorpay
    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
