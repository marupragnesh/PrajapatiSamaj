package com.matrimonial.service;

import com.matrimonial.config.RazorpayProperties;
import com.matrimonial.dto.request.CreateOrderRequest;
import com.matrimonial.dto.request.VerifyPaymentRequest;
import com.matrimonial.dto.response.OrderResponse;
import com.matrimonial.dto.response.PaymentVerifyResponse;
import com.matrimonial.entity.Payment;
import com.matrimonial.entity.User;
import com.matrimonial.entity.enums.PaymentFeature;
import com.matrimonial.entity.enums.PaymentStatus;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.repository.PaymentRepository;
import com.matrimonial.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.EnumMap;
import java.util.Map;

/**
 * SERVICE: PaymentService
 *
 * Handles all Razorpay payment business logic:
 *   - createOrder()   — creates a Razorpay order for a given feature, saves
 *                       a CREATED Payment row, amount looked up server-side
 *   - verifyPayment() — checks the HMAC-SHA256 signature Razorpay returns,
 *                       marks the Payment row SUCCESS or FAILED accordingly
 *   - hasUnlocked()   — the single reusable gate method. Any current or
 *                       future locked feature calls this instead of writing
 *                       its own payment-checking logic (e.g. ProfileMapper
 *                       uses this to decide whether to mask a mobile number)
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;

    // Server-side price list — the frontend can never override these by
    // tampering with the request, since only `feature` is sent, not amount.
    private static final Map<PaymentFeature, Integer> AMOUNTS = new EnumMap<>(PaymentFeature.class);
    static {
        AMOUNTS.put(PaymentFeature.CONTACT_UNLOCK, 9900); // Rs 99 in paise
    }

    private static final String CURRENCY = "INR";

    /**
     * Create a Razorpay order for the requested feature.
     * Saves a CREATED Payment row immediately so an order is trackable
     * even if the user closes the checkout modal without paying.
     */
    @Transactional
    public OrderResponse createOrder(String email, CreateOrderRequest request) {
        User user = getUserByEmail(email);

        Integer amount = AMOUNTS.get(request.getFeature());
        if (amount == null) {
            throw new BadRequestException("Unknown payment feature.");
        }
        if (amount < 100) {
            // Razorpay's own minimum — guards against a future misconfigured price
            throw new BadRequestException("Amount must be at least 100 paise.");
        }

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount);
        orderRequest.put("currency", CURRENCY);
        orderRequest.put("receipt", "receipt_" + user.getId() + "_" + System.currentTimeMillis());

        String razorpayOrderId;
        try {
            Order order = razorpayClient.orders.create(orderRequest);
            razorpayOrderId = order.get("id");
        } catch (RazorpayException e) {
            log.warn("Razorpay order creation failed — userId={}, feature={}, reason={}",
                    user.getId(), request.getFeature(), e.getMessage());
            throw new BadRequestException("Could not create payment order. Please try again.");
        }

        Payment payment = Payment.builder()
                .user(user)
                .feature(request.getFeature())
                .razorpayOrderId(razorpayOrderId)
                .amount(amount)
                .status(PaymentStatus.CREATED)
                .build();
        paymentRepository.save(payment);
        log.info("Payment order created — userId={}, feature={}, orderId={}",
                user.getId(), request.getFeature(), razorpayOrderId);

        return OrderResponse.builder()
                .orderId(razorpayOrderId)
                .amount(amount)
                .currency(CURRENCY)
                .keyId(razorpayProperties.getId())
                .build();
    }

    /**
     * Verify a completed payment's signature.
     * Algorithm: HMAC-SHA256(order_id + "|" + payment_id, KEY_SECRET), compared
     * to razorpay_signature. Only marks the Payment row SUCCESS if this matches —
     * a mismatch or any missing field always results in FAILED, never SUCCESS.
     */
    @Transactional
    public PaymentVerifyResponse verifyPayment(String email, VerifyPaymentRequest request) {
        User user = getUserByEmail(email);

        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment order not found."));

        if (!payment.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This payment does not belong to your account.");
        }

        boolean isValid = isSignatureValid(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.warn("Payment signature mismatch — userId={}, orderId={}", user.getId(), request.getRazorpayOrderId());
            return PaymentVerifyResponse.builder()
                    .success(false)
                    .message("Payment verification failed.")
                    .build();
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
        log.info("Payment verified — userId={}, feature={}, orderId={}",
                user.getId(), payment.getFeature(), request.getRazorpayOrderId());

        return PaymentVerifyResponse.builder()
                .success(true)
                .message("Payment verified successfully.")
                .build();
    }

    /**
     * The reusable unlock check. Returns true if this user has AT LEAST ONE
     * successful payment for the given feature — account-wide, not tied to
     * any specific profile.
     *
     * Any current or future locked feature should call this method rather
     * than querying PaymentRepository directly, so the unlock rule only
     * ever lives in one place.
     */
    public boolean hasUnlocked(User user, PaymentFeature feature) {
        return paymentRepository.existsByUserIdAndFeatureAndStatus(user.getId(), feature, PaymentStatus.SUCCESS);
    }

    /** Overload for callers that only have the user's email (e.g. controllers using UserDetails). */
    public boolean hasUnlocked(String email, PaymentFeature feature) {
        User user = getUserByEmail(email);
        return hasUnlocked(user, feature);
    }

    /**
     * Recompute HMAC-SHA256(orderId + "|" + paymentId, keySecret) and compare
     * to the signature Razorpay sent back. Uses a constant-time comparison
     * (MessageDigest.isEqual) to avoid leaking timing information.
     */
    private boolean isSignatureValid(String orderId, String paymentId, String signature) {
        if (orderId == null || paymentId == null || signature == null) {
            return false;
        }
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return MessageDigest.isEqual(
                    hex.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.warn("Signature verification threw an exception — treating as invalid. reason={}", e.getMessage());
            return false;
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
