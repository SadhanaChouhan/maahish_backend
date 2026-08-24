package com.maahish.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final CloudinaryProperties cloudinaryProperties;
    private final StringRedisTemplate redisTemplate;

    @Value("${maahish.cors.allowed-origins:}")
    private String allowedOrigins;

    @Value("${maahish.rate-limit.backend:}")
    private String rateLimitBackend;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Override
    public void run(ApplicationArguments args) {
        validateJwtSecret();
        validateRazorpay();
        validateCors();
        validateMail();
        validateCloudinary();
        validateRateLimitBackend();
        validateRedis();
        log.info("Production configuration validation passed");
    }

    private void validateJwtSecret() {
        String secret = jwtProperties.getSecret();
        if (!StringUtils.hasText(secret) || secret.length() < MIN_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set and at least " + MIN_JWT_SECRET_LENGTH + " characters in production");
        }
        if (secret.toLowerCase().contains("dev-secret")
                || secret.toLowerCase().contains("change-in-production")
                || secret.toLowerCase().contains("change_me")
                || secret.toLowerCase().contains("your_256_bit")) {
            throw new IllegalStateException(
                    "JWT_SECRET must not use a placeholder or development default in production");
        }
    }

    private void validateRazorpay() {
        if (!StringUtils.hasText(razorpayProperties.getKeyId())
                || !razorpayProperties.getKeyId().startsWith("rzp_test_")) {
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

    private void validateMail() {
        if (!StringUtils.hasText(mailHost)) {
            throw new IllegalStateException("MAIL_HOST must be set in production");
        }
        if (!StringUtils.hasText(mailUsername)) {
            throw new IllegalStateException("MAIL_USERNAME must be set in production");
        }
        if (!StringUtils.hasText(mailPassword)) {
            throw new IllegalStateException("MAIL_PASSWORD must be set in production");
        }
    }

    private void validateCloudinary() {
        if (!StringUtils.hasText(cloudinaryProperties.getCloudName())) {
            throw new IllegalStateException("CLOUDINARY_CLOUD_NAME must be set in production");
        }
        if (!StringUtils.hasText(cloudinaryProperties.getApiKey())) {
            throw new IllegalStateException("CLOUDINARY_API_KEY must be set in production");
        }
        if (!StringUtils.hasText(cloudinaryProperties.getApiSecret())) {
            throw new IllegalStateException("CLOUDINARY_API_SECRET must be set in production");
        }
    }

    private void validateRateLimitBackend() {
        if (!"redis".equalsIgnoreCase(rateLimitBackend)) {
            throw new IllegalStateException("maahish.rate-limit.backend must be 'redis' in production");
        }
    }

    private void validateRedis() {
        try (var connection = redisTemplate.getRequiredConnectionFactory().getConnection()) {
            String pong = connection.ping();
            if (!"PONG".equalsIgnoreCase(pong)) {
                throw new IllegalStateException("Redis ping did not return PONG in production");
            }
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Redis is required in production but is unavailable: " + ex.getMessage(), ex);
        }
    }
}
