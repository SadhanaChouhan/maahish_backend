package com.maahish.controller;

import com.maahish.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Payment provider webhooks")
public class RazorpayWebhookController {

    private final OrderService orderService;

    @PostMapping("/razorpay")
    @Operation(summary = "Razorpay payment webhook (payment.captured)")
    public ResponseEntity<Void> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {
        try {
            orderService.processRazorpayWebhook(payload, signature != null ? signature : "");
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("event=checkout_webhook_failed error={}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
