package com.maahish.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProductionStartupValidator implements ApplicationRunner {

    private static final int MIN_JWT_SECRET_LENGTH = 32;

    private final JwtProperties jwtProperties;
    private final RazorpayProperties razorpayProperties;

    @Value("${maahish.cors.allowed-origins:}")
    private String allowedOrigins;

    @Override
    public void run(ApplicationArguments args) {
        validateJwtSecret();
        validateRazorpay();
        validateCors();
        log.info("Production configuration validation passed");
    }

    private void validateJwtSecret() {
        String secret = jwtProperties.getSecret();
        if (!StringUtils.hasText(secret) || secret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set and at least " + MIN_JWT_SECRET_LENGTH + " characters in production");
        }
        if (secret.toLowerCase().contains("dev-secret") || secret.toLowerCase().contains("change-in-production")) {
            throw new IllegalStateException("JWT_SECRET must not use a development default in production");
        }
    }

    private void validateRazorpay() {
        if (!StringUtils.hasText(razorpayProperties.getKeyId())
                || !razorpayProperties.getKeyId().startsWith("rzp_live_")) {
            throw new IllegalStateException("RAZORPAY_KEY_ID must be a live key (rzp_live_...) in production");
        }
        if (!StringUtils.hasText(razorpayProperties.getKeySecret())) {
            throw new IllegalStateException("RAZORPAY_KEY_SECRET must be set in production");
        }
        if (!StringUtils.hasText(razorpayProperties.getWebhookSecret())) {
            throw new IllegalStateException("RAZORPAY_WEBHOOK_SECRET must be set in production");
        }
    }

    private void validateCors() {
        if (!StringUtils.hasText(allowedOrigins) || allowedOrigins.contains("localhost")) {
            throw new IllegalStateException(
                    "CORS_ALLOWED_ORIGINS must be set to your production domain(s) and must not include localhost");
        }
    }
}
