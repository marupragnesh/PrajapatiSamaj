package com.matrimonial.entity.enums;

/**
 * ENUM: PaymentStatus
 *
 * CREATED — Razorpay order created, waiting for the user to complete checkout
 * SUCCESS — signature verified, payment is genuine and confirmed
 * FAILED  — signature verification failed, or payment was never completed
 */
public enum PaymentStatus {
    CREATED,
    SUCCESS,
    FAILED
}
