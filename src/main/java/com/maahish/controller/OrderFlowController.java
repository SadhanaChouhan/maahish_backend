package com.maahish.controller;

import com.maahish.dto.request.CheckoutRequest;
import com.maahish.dto.request.VerifyPaymentRequest;
import com.maahish.dto.response.ApiResponse;
import com.maahish.dto.response.CheckoutPreviewResponse;
import com.maahish.dto.response.OrderResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.dto.response.PaymentResponse;
import com.maahish.security.SecurityUtil;
import com.maahish.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Checkout & Orders", description = "Checkout, payment, and order tracking")
public class OrderFlowController {

    private final OrderService orderService;

    @PostMapping("/v1/checkout")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Validate cart and address before payment")
    public ResponseEntity<ApiResponse<CheckoutPreviewResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.prepareCheckout(SecurityUtil.getCurrentUserId(), request)));
    }

    @PostMapping("/v1/payments/initiate")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create Razorpay payment order from cart")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.initiatePayment(SecurityUtil.getCurrentUserId(), request)));
    }

    @PostMapping("/v1/payments/verify")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Verify Razorpay payment and create order")
    public ResponseEntity<ApiResponse<OrderResponse>> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.verifyPayment(SecurityUtil.getCurrentUserId(), request)));
    }

    @GetMapping("/v1/orders")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "List user orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getUserOrders(SecurityUtil.getCurrentUserId(), page, size)));
    }

    @GetMapping("/v1/orders/{orderNumber}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrderByNumber(SecurityUtil.getCurrentUserId(), orderNumber)));
    }

    @GetMapping("/v1/orders/track/{orderNumber}")
    @Operation(summary = "Track order by order number")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(orderService.trackOrder(orderNumber)));
    }
}
