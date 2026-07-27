package com.maahish.returns.entity;

import com.maahish.infrastructure.storage.service.CloudinaryService;
import com.maahish.notification.service.MarketplaceNotificationService;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.order.repository.OrderItemRepository;
import com.maahish.order.enums.OrderStatus;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.catalog.service.ProductStockService;
import com.maahish.infrastructure.payment.service.RazorpayService;
import com.maahish.returns.repository.RefundTransactionRepository;
import com.maahish.returns.dto.response.ReturnEligibilityResponse;
import com.maahish.returns.mapper.ReturnRequestMapper;
import com.maahish.returns.repository.ReturnRequestRepository;
import com.maahish.returns.service.ReturnRequestServiceImpl;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.user.entity.User;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReturnRequestServiceImplEligibilityTest {

    @Mock private ReturnRequestRepository returnRequestRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private SellerRepository sellerRepository;
    @Mock private RefundTransactionRepository refundTransactionRepository;
    @Mock private ReturnRequestMapper returnRequestMapper;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private RazorpayService razorpayService;
    @Mock private ProductStockService productStockService;
    @Mock private MarketplaceNotificationService marketplaceNotificationService;

    @InjectMocks
    private ReturnRequestServiceImpl service;

    @Test
    void checkEligibility_whenDeliveredWithinWindow_isEligible() {
        User user = User.builder().id(1L).build();
        Order order = Order.builder()
                .id(10L)
                .status(OrderStatus.DELIVERED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .deliveredAt(LocalDateTime.now().minusDays(2))
                .user(user)
                .build();
        OrderItem item = OrderItem.builder().id(5L).order(order).build();

        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(returnRequestRepository.findByOrderItemId(5L)).thenReturn(Optional.empty());

        ReturnEligibilityResponse result = service.checkEligibility(1L, 5L);

        assertTrue(result.isEligible());
        assertTrue(result.isCanReturn());
        assertTrue(result.isCanExchange());
    }

    @Test
    void checkEligibility_whenNotDelivered_isNotEligible() {
        User user = User.builder().id(1L).build();
        Order order = Order.builder()
                .id(10L)
                .status(OrderStatus.SHIPPED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .user(user)
                .build();
        OrderItem item = OrderItem.builder().id(5L).order(order).build();

        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(returnRequestRepository.findByOrderItemId(5L)).thenReturn(Optional.empty());

        ReturnEligibilityResponse result = service.checkEligibility(1L, 5L);

        assertFalse(result.isEligible());
        assertEquals("Return/exchange is only available after delivery", result.getReason());
    }

    @Test
    void checkEligibility_whenWindowExpired_isNotEligible() {
        User user = User.builder().id(1L).build();
        Order order = Order.builder()
                .id(10L)
                .status(OrderStatus.DELIVERED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .deliveredAt(LocalDateTime.now().minusDays(10))
                .user(user)
                .build();
        OrderItem item = OrderItem.builder().id(5L).order(order).build();

        when(orderItemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(returnRequestRepository.findByOrderItemId(5L)).thenReturn(Optional.empty());

        ReturnEligibilityResponse result = service.checkEligibility(1L, 5L);

        assertFalse(result.isEligible());
        assertEquals("Return/exchange window of 7 days has expired", result.getReason());
    }
}
