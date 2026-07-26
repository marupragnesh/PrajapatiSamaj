package com.matrimonial.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CONFIG: RazorpayProperties
 *
 * Holds and binds Razorpay API keys from application.properties.
 * Uses both @ConfigurationProperties and @Value defaults to guarantee
 * key access even if property resolution varies.
 *
 * Layer: Config
 */
@Component
@ConfigurationProperties(prefix = "razorpay.key")
@Getter
@Setter
public class RazorpayProperties {

    /**
     * Razorpay Key ID (mapped from razorpay.key.id)
     */
    @Value("${razorpay.key.id:}")
    private String id;

    /**
     * Razorpay Key Secret (mapped from razorpay.key.secret)
     */
    @Value("${razorpay.key.secret:}")
    private String secret;
}
