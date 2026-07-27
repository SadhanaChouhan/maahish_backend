package com.maahish.order.entity;

import com.maahish.user.entity.Address;
import com.maahish.user.repository.AddressRepository;
import com.maahish.cart.repository.CartRepository;
import com.maahish.cart.service.CartService;
import com.maahish.order.util.CheckoutFlowLogger;
import com.maahish.notification.service.MarketplaceNotificationService;
import com.maahish.order.mapper.OrderMapper;
import com.maahish.order.mapper.OrderMappingHelper;
import com.maahish.order.service.OrderNumberGenerator;
import com.maahish.order.repository.OrderRepository;
import com.maahish.order.repository.OrderSellerAcknowledgementRepository;
import com.maahish.order.service.OrderServiceImpl;
import com.maahish.order.enums.OrderStatus;
import com.maahish.order.dto.request.OrderTrackRequest;
import com.maahish.config.OrderTrackingProperties;
import com.maahish.payment.repository.PaymentRepository;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.order.repository.PendingCheckoutRepository;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.service.ProductStockService;
import com.maahish.auth.util.RateLimitService;
import com.maahish.infrastructure.payment.service.RazorpayService;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.settlement.service.SettlementService;
import com.maahish.common.security.ShoppingAccessValidator;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTrackTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PendingCheckoutRepository pendingCheckoutRepository;
    @Mock private OrderSellerAcknowledgementRepository orderSellerAcknowledgementRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private CartService cartService;
    @Mock private OrderNumberGenerator orderNumberGenerator;
    @Mock private RazorpayService razorpayService;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderMappingHelper orderMappingHelper;
    @Mock private ProductMapper productMapper;
    @Mock private MarketplaceNotificationService marketplaceNotificationService;
    @Mock private SettlementService settlementService;
    @Mock private ShoppingAccessValidator shoppingAccessValidator;
    @Mock private CheckoutFlowLogger checkoutFlowLogger;
    @Mock private ProductStockService productStockService;
    @Mock private RateLimitService rateLimitService;

    private OrderTrackingProperties orderTrackingProperties;

    private OrderServiceImpl orderService;

    @BeforeEach
    void initProperties() {
        orderTrackingProperties = new OrderTrackingProperties();
        orderTrackingProperties.setMaxAttemptsPerHour(30);
        orderService = new OrderServiceImpl(
                orderRepository,
                pendingCheckoutRepository,
                orderSellerAcknowledgementRepository,
                paymentRepository,
                cartRepository,
                addressRepository,
                productRepository,
                userRepository,
                cartService,
                orderNumberGenerator,
                razorpayService,
                orderMapper,
                orderMappingHelper,
                productMapper,
                marketplaceNotificationService,
                settlementService,
                shoppingAccessValidator,
                checkoutFlowLogger,
                productStockService,
                orderTrackingProperties,
                rateLimitService
        );
    }

    @Test
    void trackOrder_wrongContact_throwsNotFound() {
        Order order = buildOrder("user@test.com", "9876543210");
        when(orderRepository.findWithDetailsByOrderNumber("ORD123")).thenReturn(Optional.of(order));

        OrderTrackRequest request = new OrderTrackRequest();
        request.setOrderNumber("ORD123");
        request.setContact("wrong@test.com");

        assertThrows(ResourceNotFoundException.class, () -> orderService.trackOrder(request));
    }

    @Test
    void trackOrder_validContact_returnsLimitedResponse() {
        Order order = buildOrder("user@test.com", "9876543210");
        when(orderRepository.findWithDetailsByOrderNumber("ORD123")).thenReturn(Optional.of(order));

        OrderTrackRequest request = new OrderTrackRequest();
        request.setOrderNumber("ORD123");
        request.setContact("user@test.com");

        var response = orderService.trackOrder(request);

        assertEquals("ORD123", response.getOrderNumber());
        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals("Silk Saree", response.getItems().get(0).getProductName());
        verify(rateLimitService).assertAllowed(eq("order-track:ord123"), eq(30), any());
    }

    private Order buildOrder(String email, String mobile) {
        User user = User.builder().email(email).mobile(mobile).build();
        Address address = Address.builder().mobile(mobile).build();
        OrderItem item = OrderItem.builder().productName("Silk Saree").qty(1).build();
        Order order = Order.builder()
                .orderNumber("ORD123")
                .user(user)
                .address(address)
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .trackingNumber("TRACK1")
                .items(List.of(item))
                .build();
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
