package com.maahish.order.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.order.dto.response.OrderResponse;

/**
 * Outcome of attempting to fulfill a pending checkout after payment capture.
 */
public record FulfillmentResult(Disposition disposition, OrderResponse order, String message) {

    public enum Disposition {
        /** New order created successfully. */
        SUCCESS,
        /** Checkout was already completed; existing order returned. */
        IDEMPOTENT,
        /** Stock unavailable; payment refunded automatically. */
        REFUNDED,
        /** Invalid checkout state; no retry will help. */
        REJECTED,
        /** Transient or refund failure; webhook should retry. */
        RETRYABLE
    }

    public static FulfillmentResult success(OrderResponse order) {
        return new FulfillmentResult(Disposition.SUCCESS, order, null);
    }

    public static FulfillmentResult idempotent(OrderResponse order) {
        return new FulfillmentResult(Disposition.IDEMPOTENT, order, null);
    }

    public static FulfillmentResult refunded(String message) {
        return new FulfillmentResult(Disposition.REFUNDED, null, message);
    }

    public static FulfillmentResult rejected(String message) {
        return new FulfillmentResult(Disposition.REJECTED, null, message);
    }

    public static FulfillmentResult retryable(String message) {
        return new FulfillmentResult(Disposition.RETRYABLE, null, message);
    }

    public OrderResponse toOrderResponseForCallback() {
        return switch (disposition) {
            case SUCCESS, IDEMPOTENT -> order;
            case REFUNDED, REJECTED, RETRYABLE -> throw new BadRequestException(
                    message != null ? message : "Checkout fulfillment failed");
        };
    }

    public boolean webhookShouldRetry() {
        return disposition == Disposition.RETRYABLE;
    }
}
