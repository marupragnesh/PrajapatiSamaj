package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: PaymentVerifyResponse
 *
 * Returned by POST /api/payments/verify.
 * success=true only when the HMAC-SHA256 signature genuinely matches —
 * never set true on a signature mismatch, missing fields, or exception.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVerifyResponse {

    private boolean success;

    private String message;
}
