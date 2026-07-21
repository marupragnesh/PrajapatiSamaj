package com.matrimonial.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CONFIG: RazorpayConfig
 *
 * Builds a single RazorpayClient bean from RazorpayProperties.
 * PaymentService injects this bean to call the Razorpay Orders API.
 *
 * Layer: Config (bean wiring — not business logic)
 */
@Configuration
@RequiredArgsConstructor
public class RazorpayConfig {

    private final RazorpayProperties razorpayProperties;

    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(razorpayProperties.getId(), razorpayProperties.getSecret());
    }
}
