package com.maahish.order.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Structured logs for the checkout / payment pipeline.
 * Format: event=&lt;name&gt; key=value ... — easy to filter in CloudWatch, ELK, Grafana Loki.
 */
@Slf4j
@Component
public class CheckoutFlowLogger {

    public void prepareCheckout(Long userId, Long addressId, int itemCount, BigDecimal total) {
        log.info("event=checkout_prepare userId={} addressId={} itemCount={} total={}",
                userId, addressId, itemCount, total);
    }

    public void paymentInitiated(Long userId, String checkoutReference, String razorpayOrderId,
                                 int itemCount, BigDecimal total, int expiredSessions) {
        log.info("event=checkout_payment_initiate userId={} checkoutReference={} razorpayOrderId={} itemCount={} total={} expiredSessions={}",
                userId, checkoutReference, razorpayOrderId, itemCount, total, expiredSessions);
    }

    public void paymentVerifyStarted(Long userId, String razorpayOrderId, String razorpayPaymentId) {
        log.info("event=checkout_payment_verify_start userId={} razorpayOrderId={} razorpayPaymentId={}",
                userId, razorpayOrderId, maskPaymentId(razorpayPaymentId));
    }

    public void paymentVerifyDenied(Long userId, String razorpayOrderId, Long checkoutOwnerId) {
        log.warn("event=checkout_payment_verify_denied userId={} razorpayOrderId={} checkoutOwnerId={}",
                userId, razorpayOrderId, checkoutOwnerId);
    }

    public void webhookReceived(String eventType, boolean signaturePresent) {
        log.info("event=checkout_webhook_received eventType={} signaturePresent={}",
                eventType, signaturePresent);
    }

    public void webhookIgnored(String eventType) {
        log.info("event=checkout_webhook_ignored eventType={}", eventType);
    }

    public void webhookProcessing(String eventType, String razorpayOrderId, String razorpayPaymentId) {
        log.info("event=checkout_webhook_processing eventType={} razorpayOrderId={} razorpayPaymentId={}",
                eventType, razorpayOrderId, maskPaymentId(razorpayPaymentId));
    }

    public void fulfillIdempotent(String checkoutReference, String orderNumber, String source) {
        log.info("event=checkout_fulfill_idempotent checkoutReference={} orderNumber={} source={}",
                checkoutReference, orderNumber, source);
    }

    public void fulfillInvalidStatus(String checkoutReference, String status, String source) {
        log.warn("event=checkout_fulfill_invalid_status checkoutReference={} status={} source={}",
                checkoutReference, status, source);
    }

    public void fulfillExpired(String checkoutReference, String source) {
        log.warn("event=checkout_fulfill_expired checkoutReference={} source={}",
                checkoutReference, source);
    }

    public void fulfillStockFailed(String checkoutReference, Long productId, String productName, String source) {
        log.error("event=checkout_fulfill_stock_failed checkoutReference={} productId={} productName={} source={}",
                checkoutReference, productId, productName, source);
    }

    public void fulfillSuccess(String checkoutReference, String orderNumber, Long userId,
                               BigDecimal total, int itemCount, String source) {
        log.info("event=checkout_fulfill_success checkoutReference={} orderNumber={} userId={} total={} itemCount={} source={}",
                checkoutReference, orderNumber, userId, total, itemCount, source);
    }

    public void checkoutSessionNotFound(String razorpayOrderId, String source) {
        log.warn("event=checkout_session_not_found razorpayOrderId={} source={}", razorpayOrderId, source);
    }

    private String maskPaymentId(String paymentId) {
        if (paymentId == null || paymentId.length() <= 8) {
            return paymentId;
        }
        return paymentId.substring(0, 4) + "..." + paymentId.substring(paymentId.length() - 4);
    }
}
