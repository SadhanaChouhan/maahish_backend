package com.maahish.service.impl;

import com.maahish.dto.request.BuyNowItemRequest;
import com.maahish.dto.request.AdminOrderStatusUpdateRequest;
import com.maahish.dto.request.CheckoutRequest;
import com.maahish.dto.request.VerifyPaymentRequest;
import com.maahish.dto.response.AdminOrderDetailResponse;
import com.maahish.dto.response.CheckoutPreviewResponse;
import com.maahish.dto.response.OrderResponse;
import com.maahish.dto.response.PageResponse;
import com.maahish.dto.response.PaymentResponse;
import com.maahish.entity.*;
import com.maahish.enums.OrderStatus;
import com.maahish.enums.PendingCheckoutStatus;
import com.maahish.enums.NotificationReferenceType;
import com.maahish.enums.NotificationType;
import com.maahish.enums.PaymentMethod;
import com.maahish.enums.PaymentStatus;
import com.maahish.enums.ProductStatus;
import com.maahish.enums.SellerStatus;
import com.maahish.enums.UserRole;
import com.maahish.exception.BadRequestException;
import com.maahish.exception.ForbiddenException;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.mapper.OrderMapper;
import com.maahish.mapper.OrderMappingHelper;
import com.maahish.mapper.ProductMapper;
import com.maahish.payment.RazorpayService;
import com.maahish.repository.*;
import com.maahish.security.ShoppingAccessValidator;
import com.maahish.service.CartService;
import com.maahish.service.NotificationService;
import com.maahish.service.OrderNumberGenerator;
import com.maahish.service.OrderService;
import com.maahish.service.SettlementService;
import com.maahish.sms.SmsService;
import com.maahish.util.CheckoutFlowLogger;
import com.maahish.util.PageMapper;
import com.maahish.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private static final Set<SellerStatus> SELLER_ORDER_IN_APP_STATUSES = Set.of(SellerStatus.APPROVED, SellerStatus.ACTIVE);

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
    private final NotificationService notificationService;
    private final SettlementService settlementService;
    private final SmsService smsService;
    private final ShoppingAccessValidator shoppingAccessValidator;
    private final CheckoutFlowLogger checkoutFlowLogger;

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
            Product product = pendingItem.getProduct();
            product.setStock(product.getStock() - pendingItem.getQty());
            productRepository.save(product);

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
        notifyAdminsForNewOrder(order);
        notifySellersForNewOrder(order);

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
    public OrderResponse trackOrder(String orderNumber) {
        Order order = orderRepository.findWithDetailsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        OrderResponse response = orderMappingHelper.toOrderResponse(order);
        response.setOrderNotes(null);
        return response;
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
        order = orderRepository.save(order);

        if (newStatus == OrderStatus.SHIPPED && previousStatus != OrderStatus.SHIPPED) {
            notifyCustomerOrderShipped(order);
        } else if (newStatus == OrderStatus.OUT_FOR_DELIVERY && previousStatus != OrderStatus.OUT_FOR_DELIVERY) {
            notifyCustomerOutForDelivery(order);
        } else if (newStatus == OrderStatus.DELIVERED && previousStatus != OrderStatus.DELIVERED) {
            notifyCustomerDelivered(order);
            notifySellersOrderDelivered(order);
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

    private void notifyCustomerOrderShipped(Order order) {
        User customer = order.getUser();
        String mobile = customer.getMobile() != null ? customer.getMobile() : order.getAddress().getMobile();
        StringBuilder message = new StringBuilder("Your Maahish order ")
                .append(order.getOrderNumber())
                .append(" has been shipped.");
        if (order.getTrackingNumber() != null && !order.getTrackingNumber().isBlank()) {
            message.append(" Tracking: ").append(order.getTrackingNumber()).append(".");
        }
        smsService.send(mobile, "Maahish: " + message);
    }

    private void notifyCustomerOutForDelivery(Order order) {
        User customer = order.getUser();
        String mobile = customer.getMobile() != null ? customer.getMobile() : order.getAddress().getMobile();
        smsService.send(mobile, "Maahish: Your order " + order.getOrderNumber() + " is out for delivery.");
    }

    private void notifyCustomerDelivered(Order order) {
        User customer = order.getUser();
        String mobile = customer.getMobile() != null ? customer.getMobile() : order.getAddress().getMobile();
        smsService.send(mobile, "Maahish: Your order " + order.getOrderNumber() + " has been delivered. Thank you for shopping with us!");
    }

    private void notifyAdminsForNewOrder(Order order) {
        String message = "New order " + order.getOrderNumber() + " placed by "
                + order.getUser().getName() + ". Total: Rs " + order.getTotal() + ".";
        notificationService.notifyAdmins(
                NotificationType.NEW_ORDER,
                "New order placed",
                message,
                NotificationReferenceType.ORDER,
                order.getId()
        );
        userRepository.findByRole(UserRole.ROLE_ADMIN).forEach(admin -> {
            if (admin.getMobile() != null && !admin.getMobile().isBlank()) {
                smsService.send(admin.getMobile(), "Maahish Admin: " + message);
            }
        });
    }

    private void notifySellersForNewOrder(Order order) {
        Set<Long> notifiedSellerIds = new HashSet<>();
        for (OrderItem item : order.getItems()) {
            Seller seller = item.getProduct() != null ? item.getProduct().getSeller() : null;
            if (seller == null || Boolean.TRUE.equals(seller.getPlatformOwned())
                    || !notifiedSellerIds.add(seller.getId())) {
                continue;
            }
            if (!SELLER_ORDER_IN_APP_STATUSES.contains(seller.getStatus())) {
                continue;
            }
            String message = "You have received a new order " + order.getOrderNumber()
                    + " from " + order.getUser().getName() + ". Please confirm it from your orders page.";
            notificationService.notifySeller(
                    seller,
                    NotificationType.NEW_ORDER,
                    "New order received",
                    message,
                    NotificationReferenceType.ORDER,
                    order.getId(),
                    false
            );
            if (seller.getStatus() == SellerStatus.ACTIVE
                    && seller.getMobile() != null && !seller.getMobile().isBlank()) {
                smsService.send(seller.getMobile(), "Maahish: " + message);
            }
        }
    }

    private void notifySellersOrderDelivered(Order order) {
        Set<Long> notifiedSellerIds = new HashSet<>();
        for (OrderItem item : order.getItems()) {
            Seller seller = item.getProduct() != null ? item.getProduct().getSeller() : null;
            if (seller == null || Boolean.TRUE.equals(seller.getPlatformOwned())
                    || !notifiedSellerIds.add(seller.getId())) {
                continue;
            }
            if (!SELLER_ORDER_IN_APP_STATUSES.contains(seller.getStatus())) {
                continue;
            }
            String message = "Order " + order.getOrderNumber()
                    + " has been delivered to the customer. This order is now complete.";
            notificationService.notifySeller(
                    seller,
                    NotificationType.ORDER_DELIVERED,
                    "Order delivered",
                    message,
                    NotificationReferenceType.ORDER,
                    order.getId(),
                    false
            );
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
