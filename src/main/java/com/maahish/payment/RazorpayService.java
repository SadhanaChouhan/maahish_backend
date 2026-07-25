package com.maahish.payment;

import com.maahish.config.RazorpayProperties;
import com.maahish.exception.BadRequestException;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayService {

    private final RazorpayProperties razorpayProperties;

    public boolean isConfigured() {
        return StringUtils.hasText(razorpayProperties.getKeyId())
                && StringUtils.hasText(razorpayProperties.getKeySecret());
    }

    public String getKeyId() {
        return razorpayProperties.getKeyId();
    }

    public RazorpayOrderResult createOrder(BigDecimal amountInr, String orderNumber) {
        validateConfigured();
        try {
            int amountPaise = toPaise(amountInr);
            RazorpayClient client = new RazorpayClient(
                    razorpayProperties.getKeyId().trim(),
                    razorpayProperties.getKeySecret().trim()
            );

            JSONObject options = new JSONObject();
            options.put("amount", amountPaise);
            options.put("currency", razorpayProperties.getCurrency());
            options.put("receipt", orderNumber);
            options.put("notes", new JSONObject().put("maahish_order", orderNumber));

            Order order = client.orders.create(options);
            String razorpayOrderId = order.get("id");

            log.info("event=razorpay_order_created razorpayOrderId={} receipt={} amountPaise={} currency={}",
                    razorpayOrderId, orderNumber, amountPaise, razorpayProperties.getCurrency());

            return new RazorpayOrderResult(
                    razorpayOrderId,
                    amountPaise,
                    razorpayProperties.getCurrency(),
                    order.toString()
            );
        } catch (RazorpayException ex) {
            log.error("event=razorpay_order_create_failed receipt={} error={}", orderNumber, ex.getMessage(), ex);
            throw new BadRequestException("Failed to create Razorpay order: " + ex.getMessage());
        }
    }

    public void verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        validateConfigured();
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            if (!Utils.verifyPaymentSignature(attributes, razorpayProperties.getKeySecret().trim())) {
                log.warn("event=razorpay_payment_verify_failed razorpayOrderId={} reason=invalid_signature",
                        razorpayOrderId);
                throw new BadRequestException("Payment verification failed: invalid signature");
            }
            log.info("event=razorpay_payment_verified razorpayOrderId={} razorpayPaymentId={}",
                    razorpayOrderId, maskId(razorpayPaymentId));
        } catch (RazorpayException ex) {
            log.error("event=razorpay_payment_verify_error razorpayOrderId={} error={}",
                    razorpayOrderId, ex.getMessage(), ex);
            throw new BadRequestException("Payment verification failed: " + ex.getMessage());
        }
    }

    public void verifyWebhookSignature(String payload, String signature) {
        if (!StringUtils.hasText(razorpayProperties.getWebhookSecret())) {
            log.warn("event=razorpay_webhook_secret_missing action=signature_skipped");
            return;
        }
        try {
            Utils.verifyWebhookSignature(payload, signature, razorpayProperties.getWebhookSecret().trim());
            log.info("event=razorpay_webhook_signature_valid");
        } catch (RazorpayException ex) {
            log.error("event=razorpay_webhook_signature_invalid error={}", ex.getMessage(), ex);
            throw new BadRequestException("Invalid webhook signature");
        }
    }

    private void validateConfigured() {
        if (!isConfigured()) {
            throw new BadRequestException(
                    "Razorpay is not configured. Set maahish.razorpay.key-id and key-secret in application-local.yml "
                            + "or RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET environment variables.");
        }
    }

    private int toPaise(BigDecimal amountInr) {
        if (amountInr == null || amountInr.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Invalid order amount for payment");
        }
        int paise = amountInr.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        return Math.max(paise, 100);
    }

    private String maskId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 4) + "..." + id.substring(id.length() - 4);
    }

    public record RazorpayOrderResult(
            String razorpayOrderId,
            int amountPaise,
            String currency,
            String rawResponse
    ) {}
}
