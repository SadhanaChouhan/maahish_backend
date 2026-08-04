package com.maahish.seller.service;

import com.maahish.admin.dto.response.AdminSellerOrderSummaryResponse;
import com.maahish.auth.util.RateLimitService;
import com.maahish.common.exception.BadRequestException;
import com.maahish.config.AuthRateLimitProperties;
import com.maahish.infrastructure.storage.service.CloudinaryService;
import com.maahish.common.dto.response.FileUploadResponse;
import com.maahish.common.exception.ForbiddenException;
import com.maahish.infrastructure.mail.service.MailService;
import com.maahish.notification.service.MarketplaceNotificationService;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.order.repository.OrderItemRepository;
import com.maahish.order.mapper.OrderMapper;
import com.maahish.order.repository.OrderRepository;
import com.maahish.order.entity.OrderSellerAcknowledgement;
import com.maahish.order.repository.OrderSellerAcknowledgementRepository;
import com.maahish.order.enums.OrderStatus;
import com.maahish.common.util.PageMapper;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PaginationUtil;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.dto.response.ProductDetailResponse;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.catalog.repository.ProductRepository;
import com.maahish.catalog.dto.request.ProductRequest;
import com.maahish.catalog.service.ProductService;
import com.maahish.catalog.enums.ProductStatus;
import com.maahish.catalog.dto.response.ProductSummaryResponse;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.seller.entity.Seller;
import com.maahish.seller.dto.response.SellerDashboardResponse;
import com.maahish.seller.mapper.SellerMapper;
import com.maahish.seller.dto.response.SellerOrderItemResponse;
import com.maahish.seller.dto.response.SellerOrderResponse;
import com.maahish.seller.dto.response.SellerOrderSummaryResponse;
import com.maahish.seller.dto.request.SellerProductPricingRequest;
import com.maahish.seller.dto.request.SellerProductStockRequest;
import com.maahish.seller.dto.request.SellerProfileUpdateRequest;
import com.maahish.seller.dto.request.SellerRegistrationRequest;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.seller.dto.response.SellerResponse;
import com.maahish.settlement.entity.SellerSettlement;
import com.maahish.settlement.repository.SellerSettlementRepository;
import com.maahish.seller.enums.SellerStatus;
import com.maahish.seller.dto.request.SellerStatusUpdateRequest;
import com.maahish.seller.dto.response.SellerSummaryResponse;
import com.maahish.settlement.util.SettlementAggregateUtil;
import com.maahish.settlement.service.SettlementService;
import com.maahish.settlement.enums.SettlementStatus;
import com.maahish.user.entity.User;
import com.maahish.user.repository.UserRepository;
import com.maahish.common.enums.UserRole;
import com.maahish.common.enums.UserStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private static final Set<SellerStatus> LOGIN_ALLOWED_STATUSES = Set.of(SellerStatus.APPROVED, SellerStatus.ACTIVE);
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int ESTIMATED_DELIVERY_DAYS = 7;

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderSellerAcknowledgementRepository orderSellerAcknowledgementRepository;
    private final SellerSettlementRepository settlementRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final MailService mailService;
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final SellerMapper sellerMapper;
    private final MarketplaceNotificationService marketplaceNotificationService;
    private final OrderMapper orderMapper;
    private final SettlementService settlementService;
    private final RateLimitService rateLimitService;
    private final AuthRateLimitProperties authRateLimitProperties;

    @Override
    @Transactional
    public SellerResponse register(SellerRegistrationRequest request,
                                   MultipartFile businessLogo,
                                   MultipartFile profileImage) {
        String email = request.getEmail().toLowerCase().trim();

        rateLimitService.assertAllowed(
                "seller-register:" + email,
                authRateLimitProperties.getMaxSellerRegisterAttemptsPerHour(),
                Duration.ofHours(1));

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }
        if (sellerRepository.existsByEmail(email)) {
            throw new BadRequestException("Seller email already registered");
        }
        if (sellerRepository.existsByMobile(request.getMobile())) {
            throw new BadRequestException("Mobile number already registered");
        }

        User user = User.builder()
                .name(request.getOwnerName().trim())
                .email(email)
                .mobile(request.getMobile().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ROLE_SELLER)
                .status(UserStatus.INACTIVE)
                .build();
        userRepository.save(user);

        Seller seller = Seller.builder()
                .user(user)
                .businessName(request.getBusinessName().trim())
                .ownerName(request.getOwnerName().trim())
                .email(email)
                .mobile(request.getMobile().trim())
                .businessAddress(request.getBusinessAddress().trim())
                .city(request.getCity().trim())
                .state(request.getState().trim())
                .pincode(request.getPincode().trim())
                .gst(trimToNull(request.getGst()))
                .pan(trimToNull(request.getPan()))
                .bankAccountHolder(request.getBankAccountHolder().trim())
                .bankAccountNumber(request.getBankAccountNumber().trim())
                .bankIfsc(request.getBankIfsc().trim().toUpperCase())
                .bankName(request.getBankName().trim())
                .upiId(trimToNull(request.getUpiId()))
                .status(SellerStatus.PENDING)
                .platformOwned(false)
                .build();

        applySellerImages(seller, businessLogo, profileImage);
        sellerRepository.save(seller);

        mailService.sendSellerRegistrationConfirmation(email, seller.getBusinessName());
        marketplaceNotificationService.notifyAdminsSellerRegistered(seller);
        log.info("Seller registration submitted: {}", email);
        return sellerMapper.toDetailedResponse(seller, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public SellerDashboardResponse getDashboard(Long userId) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);

        long totalProducts = productRepository.countBySellerId(seller.getId());
        long activeProducts = productRepository.countBySellerIdAndStatus(seller.getId(), ProductStatus.ACTIVE);
        long totalOrders = orderRepository.findOrdersContainingSellerProducts(seller.getId(), null, Pageable.unpaged()).getTotalElements();
        long pendingOrders = orderRepository.findOrdersContainingSellerProducts(seller.getId(), OrderStatus.PENDING, Pageable.unpaged()).getTotalElements();
        BigDecimal totalSales = settlementRepository.sumGrossSalesBySellerId(seller.getId());
        BigDecimal platformCommission = settlementRepository.sumCommissionBySellerId(seller.getId());
        BigDecimal netEarnings = settlementRepository.sumNetEarningsBySellerId(seller.getId());
        BigDecimal pendingPart1 = settlementRepository.sumNetAmountBySellerIdAndStatus(seller.getId(), SettlementStatus.PENDING);
        BigDecimal pendingPart2 = settlementRepository.sumNetAmountBySellerIdAndStatus(seller.getId(), SettlementStatus.PROCESSING);
        BigDecimal pendingSettlement = (pendingPart1 != null ? pendingPart1 : BigDecimal.ZERO)
                .add(pendingPart2 != null ? pendingPart2 : BigDecimal.ZERO);
        BigDecimal paidSettlement = settlementRepository.sumNetAmountBySellerIdAndStatus(seller.getId(), SettlementStatus.PAID);
        long lowStock = productRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId(), Pageable.unpaged())
                .stream()
                .filter(p -> p.getStock() != null && p.getStock() <= LOW_STOCK_THRESHOLD)
                .count();

        Page<Order> recentOrderPage = orderRepository.findOrdersContainingSellerProducts(
                seller.getId(), null, PaginationUtil.createPageable(0, 5, "createdAt", "desc"));
        List<SellerOrderSummaryResponse> recentOrders = recentOrderPage.getContent().stream()
                .map(order -> toOrderSummary(order, seller.getId()))
                .toList();

        return SellerDashboardResponse.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .totalRevenue(totalSales != null ? totalSales : BigDecimal.ZERO)
                .totalSales(totalSales != null ? totalSales : BigDecimal.ZERO)
                .platformCommission(platformCommission != null ? platformCommission : BigDecimal.ZERO)
                .netEarnings(netEarnings != null ? netEarnings : BigDecimal.ZERO)
                .pendingSettlement(pendingSettlement != null ? pendingSettlement : BigDecimal.ZERO)
                .paidSettlement(paidSettlement != null ? paidSettlement : BigDecimal.ZERO)
                .lowStockProducts(lowStock)
                .seller(sellerMapper.toSummary(seller, totalProducts))
                .recentOrders(recentOrders)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SellerResponse getProfile(Long userId) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        long productCount = productRepository.countBySellerId(seller.getId());
        return sellerMapper.toDetailedResponse(seller, productCount);
    }

    @Override
    @Transactional
    public SellerResponse updateProfile(Long userId,
                                        SellerProfileUpdateRequest request,
                                        MultipartFile businessLogo,
                                        MultipartFile profileImage) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);

        if (request.getBusinessName() != null) seller.setBusinessName(request.getBusinessName().trim());
        if (request.getOwnerName() != null) {
            seller.setOwnerName(request.getOwnerName().trim());
            seller.getUser().setName(request.getOwnerName().trim());
        }
        if (request.getMobile() != null) seller.setMobile(request.getMobile().trim());
        if (request.getBusinessAddress() != null) seller.setBusinessAddress(request.getBusinessAddress().trim());
        if (request.getCity() != null) seller.setCity(request.getCity().trim());
        if (request.getState() != null) seller.setState(request.getState().trim());
        if (request.getPincode() != null) seller.setPincode(request.getPincode().trim());
        if (request.getGst() != null) seller.setGst(trimToNull(request.getGst()));
        if (request.getPan() != null) seller.setPan(trimToNull(request.getPan()));
        if (request.getBankAccountHolder() != null) seller.setBankAccountHolder(request.getBankAccountHolder().trim());
        if (request.getBankAccountNumber() != null) seller.setBankAccountNumber(request.getBankAccountNumber().trim());
        if (request.getBankIfsc() != null) seller.setBankIfsc(request.getBankIfsc().trim().toUpperCase());
        if (request.getBankName() != null) seller.setBankName(request.getBankName().trim());
        if (request.getUpiId() != null) seller.setUpiId(trimToNull(request.getUpiId()));

        applySellerImages(seller, businessLogo, profileImage);
        sellerRepository.save(seller);

        long productCount = productRepository.countBySellerId(seller.getId());
        return sellerMapper.toDetailedResponse(seller, productCount);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductSummaryResponse> getProducts(Long userId, int page, int size) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        Pageable pageable = PaginationUtil.createPageable(page, size, "createdAt", "desc");
        Page<Product> products = productRepository.findBySellerIdOrderByCreatedAtDesc(seller.getId(), pageable);
        return PageMapper.toPageResponse(products, this::toProductSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProduct(Long userId, Long productId) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        Product product = getOwnedProduct(seller, productId);
        return productService.getProductById(product.getId());
    }

    @Override
    @Transactional
    public ProductDetailResponse createProduct(Long userId, ProductRequest request) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        return productService.createProductForSeller(seller, request);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateProduct(Long userId, Long productId, ProductRequest request) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        return productService.updateProductForSeller(seller, productId, request);
    }

    @Override
    @Transactional
    public void deleteProduct(Long userId, Long productId) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        productService.deleteProductForSeller(seller, productId);
    }

    @Override
    @Transactional
    public ProductDetailResponse updateStock(Long userId, Long productId, SellerProductStockRequest request) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        Product product = getOwnedProduct(seller, productId);
        product.setStock(request.getStock());
        if (request.getStock() <= 0) {
            marketplaceNotificationService.notifySellerOutOfStock(seller, product.getName(), product.getId());
        }
        productRepository.save(product);
        return productService.getProductById(product.getId());
    }

    @Override
    @Transactional
    public ProductDetailResponse updatePricing(Long userId, Long productId, SellerProductPricingRequest request) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        Product product = getOwnedProduct(seller, productId);
        product.setPrice(request.getPrice());
        product.setDiscount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO);
        product.setSellingPrice(calculateSellingPrice(request.getPrice(), product.getDiscount()));
        productRepository.save(product);
        return productService.getProductById(product.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public FileUploadResponse uploadProductImage(Long userId, MultipartFile file) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        return cloudinaryService.uploadProductImage(file);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerOrderResponse> getOrders(Long userId, OrderStatus status, int page, int size) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        Pageable pageable = PaginationUtil.createPageable(page, size, "createdAt", "desc");
        Page<Order> orders = orderRepository.findOrdersContainingSellerProducts(seller.getId(), status, pageable);
        return PageMapper.toPageResponse(orders, order -> toSellerOrder(order, seller.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public SellerOrderResponse getOrder(Long userId, Long orderId) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItem> sellerItems = order.getItems().stream()
                .filter(item -> item.getProduct().getSeller() != null
                        && item.getProduct().getSeller().getId().equals(seller.getId()))
                .toList();
        if (sellerItems.isEmpty()) {
            throw new ForbiddenException("You do not have access to this order");
        }
        return toSellerOrder(order, seller.getId());
    }

    @Override
    @Transactional
    public void confirmOrder(Long userId, Long orderId) {
        Seller seller = getSellerByUserId(userId);
        assertSellerCanAccessDashboard(seller);
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw new BadRequestException("Only active sellers can confirm orders");
        }

        Order order = orderRepository.findWithDetailsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean hasSellerItems = order.getItems().stream()
                .anyMatch(item -> item.getProduct().getSeller() != null
                        && item.getProduct().getSeller().getId().equals(seller.getId()));
        if (!hasSellerItems) {
            throw new ForbiddenException("You do not have access to this order");
        }

        if (orderSellerAcknowledgementRepository.existsByOrderIdAndSellerId(orderId, seller.getId())) {
            throw new BadRequestException("You have already confirmed this order");
        }

        orderSellerAcknowledgementRepository.save(OrderSellerAcknowledgement.builder()
                .order(order)
                .seller(seller)
                .confirmedAt(java.time.LocalDateTime.now())
                .build());

        marketplaceNotificationService.notifyCustomerOrderConfirmed(order, seller);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerSummaryResponse> adminSearchSellers(SellerStatus status, String keyword, int page, int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, "createdAt", "desc");
        Page<Seller> sellers = sellerRepository.searchSellers(status, keyword, pageable);
        return PageMapper.toPageResponse(sellers, seller -> {
            long count = productRepository.countBySellerId(seller.getId());
            return sellerMapper.toSummary(seller, count);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public SellerResponse adminGetSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        long productCount = productRepository.countBySellerId(seller.getId());
        long totalOrders = orderItemRepository.countDistinctOrdersBySellerId(seller.getId());
        BigDecimal totalSales = settlementRepository.sumGrossSalesBySellerId(seller.getId());
        if (totalSales == null || totalSales.compareTo(BigDecimal.ZERO) == 0) {
            totalSales = orderItemRepository.sumRevenueBySellerId(seller.getId());
        }
        BigDecimal totalCommission = settlementRepository.sumCommissionBySellerId(seller.getId());
        BigDecimal netEarnings = settlementRepository.sumNetEarningsBySellerId(seller.getId());
        BigDecimal pendingPart1 = settlementRepository.sumNetAmountBySellerIdAndStatus(seller.getId(), SettlementStatus.PENDING);
        BigDecimal pendingPart2 = settlementRepository.sumNetAmountBySellerIdAndStatus(seller.getId(), SettlementStatus.PROCESSING);
        BigDecimal pendingSettlement = (pendingPart1 != null ? pendingPart1 : BigDecimal.ZERO)
                .add(pendingPart2 != null ? pendingPart2 : BigDecimal.ZERO);
        BigDecimal paidSettlement = settlementRepository.sumNetAmountBySellerIdAndStatus(seller.getId(), SettlementStatus.PAID);

        SellerResponse response = sellerMapper.toAdminResponse(seller, productCount);
        response.setTotalOrders(totalOrders);
        response.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);
        response.setTotalCommission(totalCommission != null ? totalCommission : BigDecimal.ZERO);
        response.setNetEarnings(netEarnings != null ? netEarnings : BigDecimal.ZERO);
        response.setPendingSettlementAmount(pendingSettlement);
        response.setPaidSettlementAmount(paidSettlement != null ? paidSettlement : BigDecimal.ZERO);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminSellerOrderSummaryResponse> adminGetSellerOrders(Long sellerId,
                                                                              OrderStatus status,
                                                                              int page,
                                                                              int size) {
        if (!sellerRepository.existsById(sellerId)) {
            throw new ResourceNotFoundException("Seller not found");
        }
        Pageable pageable = PaginationUtil.createPageable(page, size, "createdAt", "desc");
        Page<Order> orders = orderRepository.findOrdersContainingSellerProducts(sellerId, status, pageable);
        return PageMapper.toPageResponse(orders, order -> settlementService.buildAdminSellerOrderSummary(order, sellerId));
    }

    @Override
    @Transactional
    public SellerResponse adminUpdateSellerStatus(Long sellerId, SellerStatusUpdateRequest request) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (Boolean.TRUE.equals(seller.getPlatformOwned())) {
            throw new BadRequestException("Platform seller status cannot be changed");
        }

        SellerStatus newStatus = request.getStatus();
        seller.setStatus(newStatus);
        seller.setRejectionReason(newStatus == SellerStatus.REJECTED ? request.getRejectionReason() : null);

        User user = seller.getUser();
        switch (newStatus) {
            case APPROVED, ACTIVE -> user.setStatus(UserStatus.ACTIVE);
            case PENDING -> user.setStatus(UserStatus.INACTIVE);
            case INACTIVE, SUSPENDED, REJECTED -> user.setStatus(UserStatus.INACTIVE);
        }

        if (newStatus == SellerStatus.APPROVED || newStatus == SellerStatus.ACTIVE) {
            marketplaceNotificationService.notifySellerApproved(seller);
        } else if (newStatus == SellerStatus.REJECTED) {
            marketplaceNotificationService.notifySellerRejected(seller, request.getRejectionReason());
        } else if (newStatus == SellerStatus.SUSPENDED || newStatus == SellerStatus.INACTIVE) {
            marketplaceNotificationService.notifySellerDeactivated(seller, newStatus, request.getRejectionReason());
        }

        sellerRepository.save(seller);
        userRepository.save(user);
        long productCount = productRepository.countBySellerId(seller.getId());
        return sellerMapper.toAdminResponse(seller, productCount);
    }

    @Override
    @Transactional
    public void adminHideProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Override
    public void assertSellerCanLogin(Seller seller) {
        if (seller == null) {
            throw new BadRequestException("Seller profile not found");
        }
        if (!LOGIN_ALLOWED_STATUSES.contains(seller.getStatus())) {
            if (seller.getStatus() == SellerStatus.PENDING) {
                throw new BadRequestException("Your seller account is pending admin approval");
            }
            if (seller.getStatus() == SellerStatus.REJECTED) {
                throw new BadRequestException("Your seller registration was rejected. Please contact support.");
            }
            throw new BadRequestException("Your seller account is not active");
        }
    }

    private Seller getSellerByUserId(Long userId) {
        return sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
    }

    private void assertSellerCanAccessDashboard(Seller seller) {
        if (seller.getUser() == null || seller.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenException("Account is not active");
        }
        if (!LOGIN_ALLOWED_STATUSES.contains(seller.getStatus())) {
            throw new ForbiddenException("Seller account is not approved for dashboard access");
        }
    }

    private Product getOwnedProduct(Seller seller, Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (product.getSeller() == null || !product.getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You do not own this product");
        }
        return product;
    }

    private ProductSummaryResponse toProductSummary(Product product) {
        ProductSummaryResponse summary = productMapper.toSummary(product);
        if (product.getSeller() != null) {
            summary.setSeller(sellerMapper.toSummary(product.getSeller()));
        }
        return summary;
    }

    private SellerOrderResponse toSellerOrder(Order order, Long sellerId) {
        List<SellerOrderItemResponse> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {
            if (item.getProduct().getSeller() == null
                    || !item.getProduct().getSeller().getId().equals(sellerId)) {
                continue;
            }
            BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
            subtotal = subtotal.add(lineTotal);
            items.add(SellerOrderItemResponse.builder()
                    .orderItemId(item.getId())
                    .productId(item.getProduct().getId())
                    .productSlug(item.getProduct().getSlug())
                    .productName(item.getProductName())
                    .productImageUrl(item.getProductImageUrl())
                    .qty(item.getQty())
                    .price(item.getPrice())
                    .lineTotal(lineTotal)
                    .build());
        }

        List<SellerSettlement> settlements = settlementRepository.findByOrderIdAndSellerIdOrderByCreatedAtAsc(
                order.getId(), sellerId);
        BigDecimal commissionAmount = SettlementAggregateUtil.sumCommission(settlements);
        BigDecimal netSellerEarnings = settlements.isEmpty() ? subtotal : SettlementAggregateUtil.sumNet(settlements);
        SettlementStatus settlementStatus = SettlementAggregateUtil.aggregateStatus(settlements);

        LocalDateTime estimatedDeliveryDate = null;
        if (order.getCreatedAt() != null
                && order.getStatus() != OrderStatus.CANCELLED
                && order.getStatus() != OrderStatus.DELIVERED) {
            estimatedDeliveryDate = order.getCreatedAt().plusDays(ESTIMATED_DELIVERY_DAYS);
        }

        return SellerOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .orderDate(order.getCreatedAt())
                .estimatedDeliveryDate(estimatedDeliveryDate)
                .customerName(order.getUser().getName())
                .sellerSubtotal(subtotal)
                .orderAmount(subtotal)
                .commissionAmount(commissionAmount)
                .netSellerEarnings(netSellerEarnings)
                .settlementStatus(settlementStatus)
                .sellerConfirmed(orderSellerAcknowledgementRepository.existsByOrderIdAndSellerId(order.getId(), sellerId))
                .orderNotes(order.getOrderNotes())
                .items(items)
                .build();
    }

    private SellerOrderSummaryResponse toOrderSummary(Order order, Long sellerId) {
        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product != null && product.getSeller() != null
                    && product.getSeller().getId().equals(sellerId)) {
                subtotal = subtotal.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQty())));
                itemCount++;
            }
        }
        return SellerOrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .orderDate(order.getCreatedAt())
                .customerName(order.getUser().getName())
                .sellerAmount(subtotal)
                .itemCount(itemCount)
                .build();
    }

    private void applySellerImages(Seller seller, MultipartFile businessLogo, MultipartFile profileImage) {
        if (businessLogo != null && !businessLogo.isEmpty()) {
            if (seller.getBusinessLogoPublicId() != null) {
                cloudinaryService.deleteImage(seller.getBusinessLogoPublicId());
            }
            FileUploadResponse upload = cloudinaryService.uploadSellerBusinessLogo(businessLogo);
            seller.setBusinessLogoUrl(upload.getUrl());
            seller.setBusinessLogoPublicId(upload.getPublicId());
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            if (seller.getProfileImagePublicId() != null) {
                cloudinaryService.deleteImage(seller.getProfileImagePublicId());
            }
            FileUploadResponse upload = cloudinaryService.uploadSellerProfileImage(profileImage);
            seller.setProfileImageUrl(upload.getUrl());
            seller.setProfileImagePublicId(upload.getPublicId());
        }
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal calculateSellingPrice(BigDecimal price, BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) == 0) {
            return price;
        }
        BigDecimal discountAmount = price.multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return price.subtract(discountAmount);
    }
}
