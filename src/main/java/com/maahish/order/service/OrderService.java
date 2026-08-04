package com.maahish.order.service;

import com.maahish.admin.dto.response.AdminOrderDetailResponse;
import com.maahish.admin.dto.request.AdminOrderStatusUpdateRequest;
import com.maahish.order.dto.response.CheckoutPreviewResponse;
import com.maahish.order.dto.request.CheckoutRequest;
import com.maahish.order.dto.response.OrderResponse;
import com.maahish.order.enums.OrderStatus;
import com.maahish.order.dto.request.OrderTrackRequest;
import com.maahish.order.dto.response.OrderTrackingResponse;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.payment.dto.response.PaymentResponse;
import com.maahish.order.dto.request.VerifyPaymentRequest;


public interface OrderService {

    CheckoutPreviewResponse prepareCheckout(Long userId, CheckoutRequest request);

    PaymentResponse initiatePayment(Long userId, CheckoutRequest request);

    PaymentResponse resumePayment(Long userId, String checkoutReference);

    OrderResponse verifyPayment(Long userId, VerifyPaymentRequest request);

    void processRazorpayWebhook(String payload, String signature);

    PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size);

    OrderResponse getOrderByNumber(Long userId, String orderNumber);

    OrderTrackingResponse trackOrder(OrderTrackRequest request, String clientIp);

    PageResponse<OrderResponse> getAllOrders(OrderStatus status, int page, int size);

    OrderResponse getOrderById(Long orderId);

    AdminOrderDetailResponse getAdminOrderDetail(Long orderId);

    OrderResponse adminUpdateOrderStatus(Long orderId, AdminOrderStatusUpdateRequest request);
}
