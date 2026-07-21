package com.matrimonial.controller;

import com.matrimonial.dto.request.CreateOrderRequest;
import com.matrimonial.dto.request.VerifyPaymentRequest;
import com.matrimonial.dto.response.OrderResponse;
import com.matrimonial.dto.response.PaymentVerifyResponse;
import com.matrimonial.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: PaymentController
 *
 * Handles all Razorpay payment HTTP endpoints:
 *   POST /api/payments/create-order  - Create a Razorpay order for a feature
 *   POST /api/payments/verify        - Verify payment signature after checkout
 *
 * Both endpoints require a valid JWT — payments are always tied to a
 * logged-in user (enforced by SecurityConfig, which permits only /api/auth/**
 * without a token).
 *
 * Layer: Controller (HTTP in/out only — no business logic)
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /** Create a Razorpay order. Amount is decided server-side from the feature, never trusted from the client. */
    @PostMapping("/create-order")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = paymentService.createOrder(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /** Verify a completed payment's Razorpay signature. Never marks a payment successful on mismatch. */
    @PostMapping("/verify")
    public ResponseEntity<PaymentVerifyResponse> verifyPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody VerifyPaymentRequest request) {

        PaymentVerifyResponse response = paymentService.verifyPayment(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
