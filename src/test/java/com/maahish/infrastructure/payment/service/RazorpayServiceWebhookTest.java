package com.maahish.infrastructure.payment.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.config.RazorpayProperties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RazorpayServiceWebhookTest {

    @Test
    void verifyWebhookSignature_whenRequiredAndMissingSecret_throws() {
        RazorpayProperties properties = new RazorpayProperties();
        properties.setRequireWebhookSecret(true);
        RazorpayService service = new RazorpayService(properties);

        assertThrows(BadRequestException.class,
                () -> service.verifyWebhookSignature("{}", "sig"));
    }

    @Test
    void verifyWebhookSignature_whenRequiredAndMissingSignature_throws() {
        RazorpayProperties properties = new RazorpayProperties();
        properties.setRequireWebhookSecret(true);
        properties.setWebhookSecret("whsec_test");
        RazorpayService service = new RazorpayService(properties);

        assertThrows(BadRequestException.class,
                () -> service.verifyWebhookSignature("{}", ""));
    }
}
