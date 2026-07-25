package com.maahish.service;

import com.maahish.dto.request.AdminOrderStatusUpdateRequest;
import com.maahish.dto.request.CheckoutRequest;
import com.maahish.dto.request.VerifyPaymentRequest;
import com.maahish.dto.response.AdminOrderDetailResponse;
import com.maahish.dto.response.CheckoutPreviewResponse;
import com.maahish.dto.response.OrderResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.dto.response.PaymentResponse;
import com.maahish.enums.OrderStatus;

public interface OrderService {

    CheckoutPreviewResponse prepareCheckout(Long userId, CheckoutRequest request);

    PaymentResponse initiatePayment(Long userId, CheckoutRequest request);

    OrderResponse verifyPayment(Long userId, VerifyPaymentRequest request);

    void processRazorpayWebhook(String payload, String signature);

    PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size);

    OrderResponse getOrderByNumber(Long userId, String orderNumber);

    OrderResponse trackOrder(String orderNumber);

    PageResponse<OrderResponse> getAllOrders(OrderStatus status, int page, int size);

    OrderResponse getOrderById(Long orderId);

    AdminOrderDetailResponse getAdminOrderDetail(Long orderId);

    OrderResponse adminUpdateOrderStatus(Long orderId, AdminOrderStatusUpdateRequest request);
}
