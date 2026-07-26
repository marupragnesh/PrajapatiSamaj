package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: PaymentStatusResponse
 *
 * Returned by GET /api/payments/status.
 * Indicates which premium features are currently unlocked for the user.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {

    private boolean contactUnlocked;

    private boolean filtersUnlocked;
}
