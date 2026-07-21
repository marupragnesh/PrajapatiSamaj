package com.matrimonial.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO: VerifyPaymentRequest
 *
 * Sent by the frontend to POST /api/payments/verify after the Razorpay
 * checkout modal succeeds. All three fields come directly from Razorpay's
 * success callback (razorpay_order_id, razorpay_payment_id, razorpay_signature).
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class VerifyPaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String razorpayOrderId;

    @NotBlank(message = "Payment ID is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Signature is required")
    private String razorpaySignature;
}
