package com.maahish.order.service;

import com.maahish.user.entity.Address;
import com.maahish.user.repository.AddressRepository;
import com.maahish.admin.dto.response.AdminOrderDetailResponse;
import com.maahish.admin.dto.request.AdminOrderStatusUpdateRequest;
import com.maahish.common.exception.BadRequestException;
import com.maahish.order.dto.request.BuyNowItemRequest;
import com.maahish.cart.entity.Cart;
import com.maahish.cart.entity.CartItem;
import com.maahish.cart.repository.CartRepository;
import com.maahish.cart.service.CartService;
import com.maahish.order.util.CheckoutFlowLogger;
import com.maahish.order.dto.response.CheckoutPreviewResponse;
import com.maahish.order.dto.request.CheckoutRequest;
import com.maahish.common.exception.ForbiddenException;
import com.maahish.notification.service.MarketplaceNotificationService;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.order.mapper.OrderMapper;
import com.maahish.order.mapper.OrderMappingHelper;
import com.maahish.order.repository.OrderRepository;
import com.maahish.order.dto.response.OrderResponse;
import com.maahish.order.repository.OrderSellerAcknowledgementRepository;
import com.maahish.order.enums.OrderStatus;
import com.maahish.order.dto.request.OrderTrackRequest;
import com.maahish.order.dto.response.OrderTrackingItemResponse;
import com.maahish.config.OrderTrackingProperties;
import com.maahish.order.dto.response.OrderTrackingResponse;
import com.maahish.common.util.PageMapper;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PaginationUtil;
import com.maahish.payment.entity.Payment;
import com.maahish.payment.enums.PaymentMethod;
import com.maahish.payment.repository.PaymentRepository;
import com.maahish.payment.dto.response.PaymentResponse;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.order.entity.PendingCheckout;
import com.maahish.order.entity.PendingCheckoutItem;
import com.maahish.order.repository.PendingCheckoutRepository;
import com.maahish.order.enums.PendingCheckoutStatus;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.catalog.service.ProductStockService;
import com.maahish.auth.util.RateLimitService;
import com.maahish.infrastructure.payment.service.RazorpayService;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.seller.entity.Seller;
import com.maahish.settlement.service.SettlementService;
import com.maahish.common.security.ShoppingAccessValidator;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;
import com.maahish.order.dto.request.VerifyPaymentRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("1999");
    private static final BigDecimal SHIPPING_CHARGE = new BigDecimal("99");
    private static final int CHECKOUT_EXPIRY_MINUTES = 30;
    private static final String FULFILL_SOURCE_CALLBACK = "callback";
    private static final String FULFILL_SOURCE_WEBHOOK = "webhook";

    private final OrderRepository orderRepository;
    private final PendingCheckoutRepository pendingCheckoutRepository;
    private final OrderSellerAcknowledgementRepository orderSellerAcknowledgementRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderNumberGenerator orderNumberGenerator;
    private final RazorpayService razorpayService;
    private final OrderMapper orderMapper;
    private final OrderMappingHelper orderMappingHelper;
    private final ProductMapper productMapper;
    private final MarketplaceNotificationService marketplaceNotificationService;
    private final SettlementService settlementService;
    private final ShoppingAccessValidator shoppingAccessValidator;
    private final CheckoutFlowLogger checkoutFlowLogger;
    private final ProductStockService productStockService;
    private final OrderTrackingProperties orderTrackingProperties;
    private final RateLimitService rateLimitService;

    @Override
    @Transactional(readOnly = true)
    public CheckoutPreviewResponse prepareCheckout(Long userId, CheckoutRequest request) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressRepository.findByIdAndUser(request.getAddressId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        List<CheckoutLine> lines = resolveCheckoutLines(userId, request);
        validateCheckoutLines(lines);
        CartTotals totals = calculateLineTotals(lines);

        checkoutFlowLogger.prepareCheckout(userId, address.getId(), lines.size(), totals.total());

        return CheckoutPreviewResponse.builder()
                .addressId(address.getId())
                .subtotal(totals.subtotal())
                .shippingCharge(totals.shipping())
                .total(totals.total())
                .message("Proceed to payment to place your order")
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(Long userId, CheckoutRequest request) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressRepository.findByIdAndUser(request.getAddressId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        List<CheckoutLine> lines = resolveCheckoutLines(userId, request);
        validateCheckoutLines(lines);
        int expiredSessions = expirePendingCheckouts(userId);

        CartTotals totals = calculateLineTotals(lines);
        String checkoutReference = UUID.randomUUID().toString().replace("-", "");
        boolean buyNow = isBuyNowCheckout(request);

        RazorpayService.RazorpayOrderResult result = razorpayService.createOrder(
                totals.total(), checkoutReference);

        PendingCheckout pendingCheckout = PendingCheckout.builder()
                .user(user)
                .address(address)
                .orderNotes(normalizeOrderNotes(request.getOrderNotes()))
                .checkoutReference(checkoutReference)
                .razorpayOrderId(result.razorpayOrderId())
                .subtotal(totals.subtotal())
                .shippingCharge(totals.shipping())
                .total(totals.total())
                .status(PendingCheckoutStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(CHECKOUT_EXPIRY_MINUTES))
                .buyNow(buyNow)
                .build();

        for (CheckoutLine line : lines) {
            Product product = line.product();
            PendingCheckoutItem item = PendingCheckoutItem.builder()
                    .pendingCheckout(pendingCheckout)
                    .product(product)
                    .qty(line.quantity())
                    .price(product.getSellingPrice())
                    .productName(product.getName())
                    .productImageUrl(productMapper.toSummary(product).getPrimaryImageUrl())
                    .build();
            pendingCheckout.getItems().add(item);
        }

        pendingCheckoutRepository.save(pendingCheckout);
        checkoutFlowLogger.paymentInitiated(
                userId, checkoutReference, result.razorpayOrderId(),
                lines.size(), totals.total(), expiredSessions);

        return PaymentResponse.builder()
                .razorpayOrderId(result.razorpayOrderId())
                .checkoutReference(checkoutReference)
                .amount(totals.total())
                .amountPaise(result.amountPaise())
                .currency(result.currency())
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.RAZORPAY)
                .razorpayKeyId(razorpayService.getKeyId())
                .customerName(user.getName())
                .customerEmail(user.getEmail())
                .customerPhone(user.getMobile())
                .build();
    }

    @Override
    @Transactional
    public OrderResponse verifyPayment(Long userId, VerifyPaymentRequest request) {
        shoppingAccessValidator.requireCustomer(userId);
        checkoutFlowLogger.paymentVerifyStarted(
                userId, request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        PendingCheckout pendingCheckout = pendingCheckoutRepository.findWithDetailsByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> {
                    checkoutFlowLogger.checkoutSessionNotFound(request.getRazorpayOrderId(), FULFILL_SOURCE_CALLBACK);
                    return new ResourceNotFoundException("Checkout session not found");
                });

        if (!pendingCheckout.getUser().getId().equals(userId)) {
            checkoutFlowLogger.paymentVerifyDenied(
                    userId, request.getRazorpayOrderId(), pendingCheckout.getUser().getId());
            throw new ForbiddenException("Access denied to this checkout session");
        }

        razorpayService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        return fulfillPendingCheckout(pendingCheckout, request.getRazorpayPaymentId(), FULFILL_SOURCE_CALLBACK);
    }

    @Override
    @Transactional
    public void processRazorpayWebhook(String payload, String signature) {
        org.json.JSONObject event = new org.json.JSONObject(payload);
        String eventType = event.optString("event");
        checkoutFlowLogger.webhookReceived(eventType, signature != null && !signature.isBlank());

        razorpayService.verifyWebhookSignature(payload, signature);

        if (!"payment.captured".equals(eventType)) {
            checkoutFlowLogger.webhookIgnored(eventType);
            return;
        }

        org.json.JSONObject paymentEntity = event.getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");
        String razorpayOrderId = paymentEntity.getString("order_id");
        String razorpayPaymentId = paymentEntity.getString("id");

        checkoutFlowLogger.webhookProcessing(eventType, razorpayOrderId, razorpayPaymentId);

        PendingCheckout pendingCheckout = pendingCheckoutRepository.findWithDetailsByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> {
                    checkoutFlowLogger.checkoutSessionNotFound(razorpayOrderId, FULFILL_SOURCE_WEBHOOK);
                    return new ResourceNotFoundException("Checkout session not found for webhook");
                });

        fulfillPendingCheckout(pendingCheckout, razorpayPaymentId, FULFILL_SOURCE_WEBHOOK);
    }

    private OrderResponse fulfillPendingCheckout(PendingCheckout pendingCheckout, String razorpayPaymentId, String source) {
        if (pendingCheckout.getStatus() == PendingCheckoutStatus.COMPLETED) {
            checkoutFlowLogger.fulfillIdempotent(
                    pendingCheckout.getCheckoutReference(), pendingCheckout.getOrderNumber(), source);
            Order existing = orderRepository.findWithDetailsByOrderNumber(pendingCheckout.getOrderNumber())
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            return orderMappingHelper.toOrderResponse(existing);
        }

        if (pendingCheckout.getStatus() != PendingCheckoutStatus.PENDING) {
            checkoutFlowLogger.fulfillInvalidStatus(
                    pendingCheckout.getCheckoutReference(), pendingCheckout.getStatus().name(), source);
            throw new BadRequestException("Checkout session is no longer valid");
        }
        if (pendingCheckout.getExpiresAt().isBefore(LocalDateTime.now())) {
            pendingCheckout.setStatus(PendingCheckoutStatus.EXPIRED);
            pendingCheckoutRepository.save(pendingCheckout);
            checkoutFlowLogger.fulfillExpired(pendingCheckout.getCheckoutReference(), source);
            throw new BadRequestException("Checkout session has expired. Please start checkout again.");
        }

        for (PendingCheckoutItem item : pendingCheckout.getItems()) {
            Product product = item.getProduct();
            if (product.getStatus() != ProductStatus.ACTIVE || product.getStock() < item.getQty()) {
                pendingCheckout.setStatus(PendingCheckoutStatus.FAILED);
                pendingCheckoutRepository.save(pendingCheckout);
                checkoutFlowLogger.fulfillStockFailed(
                        pendingCheckout.getCheckoutReference(), product.getId(), product.getName(), source);
                throw new BadRequestException("Product unavailable: " + product.getName());
            }
        }

        User user = pendingCheckout.getUser();
        String orderNumber = orderNumberGenerator.generate();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .address(pendingCheckout.getAddress())
                .subtotal(pendingCheckout.getSubtotal())
                .shippingCharge(pendingCheckout.getShippingCharge())
                .total(pendingCheckout.getTotal())
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.COMPLETED)
                .orderNotes(pendingCheckout.getOrderNotes())
                .build();

        for (PendingCheckoutItem pendingItem : pendingCheckout.getItems()) {
            Product product = productStockService.deductStock(
                    pendingItem.getProduct().getId(), pendingItem.getQty());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .qty(pendingItem.getQty())
                    .price(pendingItem.getPrice())
                    .productName(pendingItem.getProductName())
                    .productImageUrl(pendingItem.getProductImageUrl())
                    .build();
            order.getItems().add(orderItem);
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(pendingCheckout.getTotal())
                .status(PaymentStatus.COMPLETED)
                .method(PaymentMethod.RAZORPAY)
                .razorpayOrderId(pendingCheckout.getRazorpayOrderId())
                .transactionId(razorpayPaymentId)
                .build();
        order.setPayment(payment);

        order = orderRepository.save(order);

        pendingCheckout.setStatus(PendingCheckoutStatus.COMPLETED);
        pendingCheckout.setOrderNumber(orderNumber);
        pendingCheckoutRepository.save(pendingCheckout);

        if (!Boolean.TRUE.equals(pendingCheckout.getBuyNow())) {
            cartService.clearCart(user.getId());
        }
        settlementService.createSettlementsForOrder(order);
        marketplaceNotificationService.notifyNewOrder(order);

        checkoutFlowLogger.fulfillSuccess(
                pendingCheckout.getCheckoutReference(),
                orderNumber,
                user.getId(),
                pendingCheckout.getTotal(),
                order.getItems().size(),
                source);
        return orderMappingHelper.toOrderResponse(order);
    }

    private List<CheckoutLine> resolveCheckoutLines(Long userId, CheckoutRequest request) {
        if (isBuyNowCheckout(request)) {
            return resolveBuyNowLines(request.getBuyNowItems());
        }
        Cart cart = loadNonEmptyCart(userId);
        List<CheckoutLine> lines = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            lines.add(new CheckoutLine(cartItem.getProduct(), cartItem.getQuantity()));
        }
        return lines;
    }

    private boolean isBuyNowCheckout(CheckoutRequest request) {
        return request.getBuyNowItems() != null && !request.getBuyNowItems().isEmpty();
    }

    private List<CheckoutLine> resolveBuyNowLines(List<BuyNowItemRequest> buyNowItems) {
        List<CheckoutLine> lines = new ArrayList<>();
        Set<Long> seenProductIds = new HashSet<>();
        for (BuyNowItemRequest item : buyNowItems) {
            if (!seenProductIds.add(item.getProductId())) {
                throw new BadRequestException("Duplicate product in buy-now checkout");
            }
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for " + product.getName()
                        + ". Available: " + product.getStock());
            }
            lines.add(new CheckoutLine(product, item.getQuantity()));
        }
        return lines;
    }

    private void validateCheckoutLines(List<CheckoutLine> lines) {
        if (lines.isEmpty()) {
            throw new BadRequestException("No items to checkout");
        }
        for (CheckoutLine line : lines) {
            Product product = line.product();
            if (product.getStatus() != ProductStatus.ACTIVE || product.getStock() < line.quantity()) {
                throw new BadRequestException("Product unavailable: " + product.getName());
            }
        }
    }

    private CartTotals calculateLineTotals(List<CheckoutLine> lines) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CheckoutLine line : lines) {
            subtotal = subtotal.add(line.product().getSellingPrice()
                    .multiply(BigDecimal.valueOf(line.quantity())));
        }
        BigDecimal shipping = subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ? BigDecimal.ZERO : SHIPPING_CHARGE;
        return new CartTotals(subtotal, shipping, subtotal.add(shipping));
    }

    private Cart loadNonEmptyCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        return cart;
    }

    private void validateCartItems(Cart cart) {
        validateCheckoutLines(cart.getItems().stream()
                .map(item -> new CheckoutLine(item.getProduct(), item.getQuantity()))
                .toList());
    }

    private CartTotals calculateCartTotals(Cart cart) {
        return calculateLineTotals(cart.getItems().stream()
                .map(item -> new CheckoutLine(item.getProduct(), item.getQuantity()))
                .toList());
    }

    private int expirePendingCheckouts(Long userId) {
        List<PendingCheckout> pending = pendingCheckoutRepository.findByUserIdAndStatus(userId, PendingCheckoutStatus.PENDING);
        for (PendingCheckout checkout : pending) {
            checkout.setStatus(PendingCheckoutStatus.EXPIRED);
        }
        if (!pending.isEmpty()) {
            pendingCheckoutRepository.saveAll(pending);
        }
        return pending.size();
    }

    private record CheckoutLine(Product product, int quantity) {}

    private record CartTotals(BigDecimal subtotal, BigDecimal shipping, BigDecimal total) {}

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size) {
        shoppingAccessValidator.requireCustomer(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(
                user, PaginationUtil.createPageable(page, size, "createdAt", "desc"));
        return PageMapper.toPageResponse(orders, orderMappingHelper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(Long userId, String orderNumber) {
        shoppingAccessValidator.requireCustomer(userId);
        Order order = orderRepository.findWithDetailsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Access denied to this order");
        }
        return orderMappingHelper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderTrackingResponse trackOrder(OrderTrackRequest request) {
        String rateLimitKey = "order-track:" + request.getOrderNumber().trim().toLowerCase();
        rateLimitService.assertAllowed(
                rateLimitKey,
                orderTrackingProperties.getMaxAttemptsPerHour(),
                Duration.ofHours(1));

        Order order = orderRepository.findWithDetailsByOrderNumber(request.getOrderNumber().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found or contact does not match"));

        if (!contactMatchesOrder(order, request.getContact())) {
            throw new ResourceNotFoundException("Order not found or contact does not match");
        }

        return OrderTrackingResponse.builder()
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .trackingNumber(order.getTrackingNumber())
                .statusNote(order.getStatusNote())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream()
                        .map(item -> OrderTrackingItemResponse.builder()
                                .productName(item.getProductName())
                                .qty(item.getQty())
                                .build())
                        .toList())
                .build();
    }

    private boolean contactMatchesOrder(Order order, String contact) {
        if (contact == null || contact.isBlank()) {
            return false;
        }
        String normalized = normalizeContact(contact);
        User user = order.getUser();
        if (user != null) {
            if (user.getEmail() != null && normalizeContact(user.getEmail()).equals(normalized)) {
                return true;
            }
            if (user.getMobile() != null && normalizeMobile(user.getMobile()).equals(normalized)) {
                return true;
            }
        }
        Address address = order.getAddress();
        if (address != null && address.getMobile() != null
                && normalizeMobile(address.getMobile()).equals(normalized)) {
            return true;
        }
        return false;
    }

    private String normalizeContact(String value) {
        String trimmed = value.trim().toLowerCase();
        if (trimmed.contains("@")) {
            return trimmed;
        }
        return normalizeMobile(trimmed);
    }

    private String normalizeMobile(String mobile) {
        String digits = mobile.replaceAll("\\D", "");
        if (digits.length() == 12 && digits.startsWith("91")) {
            digits = digits.substring(2);
        }
        return digits;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(OrderStatus status, int page, int size) {
        Page<Order> orders = status != null
                ? orderRepository.findByStatusOrderByCreatedAtDesc(status, PaginationUtil.createPageable(page, size, "createdAt", "desc"))
                : orderRepository.findAllByOrderByCreatedAtDesc(PaginationUtil.createPageable(page, size, "createdAt", "desc"));
        return PageMapper.toPageResponse(orders, orderMappingHelper::toOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMappingHelper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminOrderDetailResponse getAdminOrderDetail(Long orderId) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderResponse orderResponse = orderMappingHelper.toOrderResponse(order);
        return AdminOrderDetailResponse.builder()
                .order(orderResponse)
                .sellerBreakdowns(settlementService.buildSellerBreakdownsForOrder(order))
                .build();
    }

    @Override
    @Transactional
    public OrderResponse adminUpdateOrderStatus(Long orderId, AdminOrderStatusUpdateRequest request) {
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPaymentStatus() != PaymentStatus.COMPLETED) {
            throw new BadRequestException("Only paid orders can be updated");
        }
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("This order can no longer be updated");
        }

        assertAllSellersConfirmed(order);

        OrderStatus newStatus = request.getStatus();
        if (newStatus != OrderStatus.SHIPPED
                && newStatus != OrderStatus.OUT_FOR_DELIVERY
                && newStatus != OrderStatus.DELIVERED) {
            throw new BadRequestException("Admin can only set status to SHIPPED, OUT_FOR_DELIVERY, or DELIVERED");
        }

        if (newStatus == OrderStatus.SHIPPED
                && (request.getTrackingNumber() == null || request.getTrackingNumber().isBlank())) {
            throw new BadRequestException("Tracking number is required when marking order as shipped");
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            order.setTrackingNumber(request.getTrackingNumber().trim());
        }
        if (request.getNote() != null && !request.getNote().isBlank()) {
            order.setStatusNote(request.getNote().trim());
        }
        if (newStatus == OrderStatus.DELIVERED && previousStatus != OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
        }
        order = orderRepository.save(order);

        if (newStatus == OrderStatus.SHIPPED && previousStatus != OrderStatus.SHIPPED) {
            marketplaceNotificationService.notifyCustomerOrderShipped(order);
        } else if (newStatus == OrderStatus.OUT_FOR_DELIVERY && previousStatus != OrderStatus.OUT_FOR_DELIVERY) {
            marketplaceNotificationService.notifyCustomerOutForDelivery(order);
        } else if (newStatus == OrderStatus.DELIVERED && previousStatus != OrderStatus.DELIVERED) {
            marketplaceNotificationService.notifyCustomerOrderDelivered(order);
            marketplaceNotificationService.notifySellersOrderDelivered(order);
        }

        log.info("event=admin_order_status_updated orderId={} orderNumber={} status={} tracking={}",
                orderId, order.getOrderNumber(), newStatus,
                order.getTrackingNumber() != null ? "set" : "none");

        return orderMappingHelper.toOrderResponse(order);
    }

    private void assertAllSellersConfirmed(Order order) {
        Set<Long> requiredSellerIds = new HashSet<>();
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product == null || product.getSeller() == null) {
                continue;
            }
            Seller seller = product.getSeller();
            if (Boolean.TRUE.equals(seller.getPlatformOwned())) {
                continue;
            }
            requiredSellerIds.add(seller.getId());
        }
        for (Long sellerId : requiredSellerIds) {
            if (!orderSellerAcknowledgementRepository.existsByOrderIdAndSellerId(order.getId(), sellerId)) {
                throw new BadRequestException(
                        "All sellers must confirm the order before shipment status can be updated");
            }
        }
    }

    private String normalizeOrderNotes(String orderNotes) {
        if (orderNotes == null || orderNotes.isBlank()) {
            return null;
        }
        String trimmed = orderNotes.trim();
        if (trimmed.length() > 500) {
            throw new BadRequestException("Order notes must not exceed 500 characters");
        }
        return trimmed;
    }
}
