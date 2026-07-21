package com.matrimonial.dto.request;

import com.matrimonial.entity.enums.PaymentFeature;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO: CreateOrderRequest
 *
 * Sent by the frontend to POST /api/payments/create-order.
 * Identifies WHICH feature the user wants to unlock — the amount is never
 * trusted from the frontend, it is looked up server-side from `feature`
 * (see PaymentService.AMOUNTS) so a tampered request cannot pay less.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "Feature is required")
    private PaymentFeature feature;
}
