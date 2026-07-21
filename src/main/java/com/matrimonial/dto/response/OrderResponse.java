package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: OrderResponse
 *
 * Returned by POST /api/payments/create-order. Gives the frontend everything
 * it needs to open the Razorpay checkout modal.
 *
 * `keyId` is the PUBLIC Razorpay key — safe to send to the frontend.
 * The SECRET key never leaves the backend.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;

    // Amount in paise, echoed back so frontend doesn't need its own copy of pricing
    private Integer amount;

    private String currency;

    // Public Razorpay Key ID — required by checkout.js to open the modal
    private String keyId;
}
