package com.matrimonial.repository;

import com.matrimonial.entity.Payment;
import com.matrimonial.entity.enums.PaymentFeature;
import com.matrimonial.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY: PaymentRepository
 *
 * Handles all DB operations for the "payments" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find a payment by the Razorpay order ID — used during verification
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    // Used by PaymentService.hasUnlocked() — true if this user has ANY
    // successful payment for the given feature (account-wide unlock check)
    boolean existsByUserIdAndFeatureAndStatus(Long userId, PaymentFeature feature, PaymentStatus status);

    // Used during account deletion to clear all payment records belonging to the user
    void deleteByUserId(Long userId);
}
