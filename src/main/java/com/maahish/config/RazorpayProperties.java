package com.maahish.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "maahish.razorpay")
public class RazorpayProperties {

    private String keyId;
    private String keySecret;
    private String webhookSecret;
    private String currency = "INR";
    /** When true, reject webhooks if webhook secret is missing or signature is invalid. */
    private boolean requireWebhookSecret = false;
}
