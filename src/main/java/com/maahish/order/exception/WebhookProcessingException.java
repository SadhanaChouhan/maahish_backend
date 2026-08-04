package com.maahish.order.exception;

/**
 * Thrown when a Razorpay webhook should be retried (transient failure or refund could not be processed).
 */
public class WebhookProcessingException extends RuntimeException {

    public WebhookProcessingException(String message) {
        super(message);
    }

    public WebhookProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
