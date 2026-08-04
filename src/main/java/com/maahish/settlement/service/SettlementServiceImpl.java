package com.maahish.settlement.service;

import com.maahish.admin.dto.response.AdminOrderBreakdownItemResponse;
import com.maahish.admin.dto.response.AdminOrderSellerBreakdownResponse;
import com.maahish.admin.dto.response.AdminSellerOrderSummaryResponse;
import com.maahish.common.exception.BadRequestException;
import com.maahish.settlement.entity.CommissionRule;
import com.maahish.order.entity.Order;
import com.maahish.order.entity.OrderItem;
import com.maahish.order.repository.OrderSellerAcknowledgementRepository;
import com.maahish.common.util.PageMapper;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PaginationUtil;
import com.maahish.catalog.entity.Product;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.seller.entity.Seller;
import com.maahish.seller.repository.SellerRepository;
import com.maahish.settlement.entity.SellerSettlement;
import com.maahish.settlement.repository.SellerSettlementRepository;
import com.maahish.seller.dto.response.SellerSettlementResponse;
import com.maahish.settlement.util.SettlementAggregateUtil;
import com.maahish.settlement.enums.SettlementStatus;
import com.maahish.settlement.dto.request.SettlementStatusUpdateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private static final BigDecimal DEFAULT_COMMISSION_PERCENTAGE = new BigDecimal("10.00");

    private final SellerSettlementRepository settlementRepository;
    private final SellerRepository sellerRepository;
    private final OrderSellerAcknowledgementRepository orderSellerAcknowledgementRepository;
    private final CommissionRuleService commissionRuleService;

    @Override
    @Transactional
    public void createSettlementsForOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            if (settlementRepository.existsByOrderItemId(item.getId())) {
                continue;
            }

            Product product = item.getProduct();
            Seller seller = product != null ? product.getSeller() : null;
            if (seller == null || Boolean.TRUE.equals(seller.getPlatformOwned())) {
                continue;
            }

            CommissionRule rule = commissionRuleService.resolveRuleForProduct(product);
            BigDecimal commissionPercentage = rule != null
                    ? rule.getCommissionPercentage()
                    : DEFAULT_COMMISSION_PERCENTAGE;

            BigDecimal grossAmount = item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQty()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal commissionAmount = grossAmount
                    .multiply(commissionPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal netSellerAmount = grossAmount.subtract(commissionAmount);

            SellerSettlement settlement = SellerSettlement.builder()
                    .seller(seller)
                    .order(order)
                    .orderItem(item)
                    .grossAmount(grossAmount)
                    .commissionPercentage(commissionPercentage)
                    .commissionAmount(commissionAmount)
                    .netSellerAmount(netSellerAmount)
                    .settlementStatus(SettlementStatus.PENDING)
                    .commissionRuleId(rule != null ? rule.getId() : null)
                    .matchedFabric(product.getFabricType() != null ? product.getFabricType().getName() : null)
                    .matchedCategoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                    .build();

            settlementRepository.save(settlement);
            log.info("event=settlement_created orderNumber={} orderItemId={} sellerId={} grossAmount={} netAmount={}",
                    order.getOrderNumber(), item.getId(), seller.getId(),
                    settlement.getGrossAmount(), settlement.getNetSellerAmount());
        }
    }

    @Override
    @Transactional
    public void cancelSettlementForReturnedOrderItem(Long orderItemId) {
        settlementRepository.findByOrderItemId(orderItemId).ifPresent(settlement -> {
            SettlementStatus status = settlement.getSettlementStatus();
            if (status == SettlementStatus.PAID) {
                log.error(
                        "event=settlement_cancel_manual_required settlementId={} orderItemId={} "
                                + "message=Settlement already paid — finance must recover from seller manually",
                        settlement.getId(), orderItemId);
                return;
            }
            if (status == SettlementStatus.PENDING || status == SettlementStatus.PROCESSING) {
                settlement.setSettlementStatus(SettlementStatus.FAILED);
                settlementRepository.save(settlement);
                log.info("event=settlement_cancelled_return settlementId={} orderItemId={}",
                        settlement.getId(), orderItemId);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerSettlementResponse> adminListSettlements(SettlementStatus status,
                                                                       Long sellerId,
                                                                       int page,
                                                                       int size) {
        Pageable pageable = PaginationUtil.createPageable(page, size, "createdAt", "desc");
        Page<SellerSettlement> settlements;
        if (sellerId != null && status != null) {
            settlements = settlementRepository.findBySellerIdAndSettlementStatusOrderByCreatedAtDesc(
                    sellerId, status, pageable);
        } else if (sellerId != null) {
            settlements = settlementRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        } else if (status != null) {
            settlements = settlementRepository.findBySettlementStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            settlements = settlementRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return PageMapper.toPageResponse(settlements, this::toResponse);
    }

    @Override
    @Transactional
    public SellerSettlementResponse adminUpdateSettlementStatus(Long settlementId,
                                                                SettlementStatusUpdateRequest request) {
        SellerSettlement settlement = settlementRepository.findWithDetailsById(settlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Settlement not found"));

        SettlementStatus newStatus = request.getStatus();
        switch (newStatus) {
            case PROCESSING -> {
                if (settlement.getSettlementStatus() != SettlementStatus.PENDING) {
                    throw new BadRequestException("Only pending settlements can be marked as processing");
                }
            }
            case PAID -> {
                if (settlement.getSettlementStatus() != SettlementStatus.PENDING
                        && settlement.getSettlementStatus() != SettlementStatus.PROCESSING) {
                    throw new BadRequestException("Only pending or processing settlements can be marked as paid");
                }
                if (request.getTransactionReference() == null || request.getTransactionReference().isBlank()) {
                    throw new BadRequestException("Transaction reference is required when marking settlement as paid");
                }
                settlement.setTransactionReference(request.getTransactionReference().trim());
                settlement.setSettlementDate(LocalDateTime.now());
            }
            case FAILED -> {
                if (settlement.getSettlementStatus() == SettlementStatus.PAID) {
                    throw new BadRequestException("Paid settlements cannot be marked as failed");
                }
            }
            case PENDING -> throw new BadRequestException("Settlements cannot be moved back to pending");
        }

        settlement.setSettlementStatus(newStatus);
        return toResponse(settlementRepository.save(settlement));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SellerSettlementResponse> sellerListSettlements(Long userId,
                                                                        SettlementStatus status,
                                                                        int page,
                                                                        int size) {
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        Page<SellerSettlement> settlements = status != null
                ? settlementRepository.findBySellerIdAndSettlementStatusOrderByCreatedAtDesc(
                seller.getId(), status, PaginationUtil.createPageable(page, size, "createdAt", "desc"))
                : settlementRepository.findBySellerIdOrderByCreatedAtDesc(
                seller.getId(), PaginationUtil.createPageable(page, size, "createdAt", "desc"));
        return PageMapper.toPageResponse(settlements, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminOrderSellerBreakdownResponse> buildSellerBreakdownsForOrder(Order order) {
        Map<Long, List<SellerSettlement>> settlementsBySeller = new LinkedHashMap<>();
        List<SellerSettlement> settlements = settlementRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
        for (SellerSettlement settlement : settlements) {
            settlementsBySeller.computeIfAbsent(settlement.getSeller().getId(), key -> new ArrayList<>()).add(settlement);
        }

        Map<Long, List<OrderItem>> itemsBySeller = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            Seller seller = product != null ? product.getSeller() : null;
            if (seller == null) {
                continue;
            }
            itemsBySeller.computeIfAbsent(seller.getId(), key -> new ArrayList<>()).add(item);
        }

        List<AdminOrderSellerBreakdownResponse> breakdowns = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : itemsBySeller.entrySet()) {
            Long sellerId = entry.getKey();
            List<SellerSettlement> sellerSettlements = settlementsBySeller.get(sellerId);
            if (sellerSettlements != null && !sellerSettlements.isEmpty()) {
                breakdowns.add(toSellerBreakdown(order.getId(), sellerSettlements));
            } else {
                Seller seller = entry.getValue().getFirst().getProduct().getSeller();
                breakdowns.add(toSellerBreakdownFromOrderItems(order.getId(), seller, entry.getValue()));
            }
        }

        return breakdowns;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSellerOrderSummaryResponse buildAdminSellerOrderSummary(Order order, Long sellerId) {
        List<SellerSettlement> settlements = settlementRepository.findByOrderIdAndSellerIdOrderByCreatedAtAsc(
                order.getId(), sellerId);

        BigDecimal productTotal = SettlementAggregateUtil.sumGross(settlements);
        BigDecimal commissionAmount = SettlementAggregateUtil.sumCommission(settlements);
        BigDecimal netSellerAmount = SettlementAggregateUtil.sumNet(settlements);
        int itemCount = settlements.size();

        if (itemCount == 0) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null && product.getSeller() != null
                        && product.getSeller().getId().equals(sellerId)) {
                    BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
                    productTotal = productTotal.add(lineTotal);
                    netSellerAmount = netSellerAmount.add(lineTotal);
                    itemCount++;
                }
            }
        }

        return AdminSellerOrderSummaryResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getCreatedAt())
                .orderStatus(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .customerName(order.getUser().getName())
                .itemCount(itemCount)
                .sellerProductTotal(productTotal)
                .commissionAmount(commissionAmount)
                .netSellerAmount(netSellerAmount)
                .settlementStatus(SettlementAggregateUtil.aggregateStatus(settlements))
                .build();
    }

    private AdminOrderSellerBreakdownResponse toSellerBreakdown(Long orderId, List<SellerSettlement> sellerSettlements) {
        Seller seller = sellerSettlements.getFirst().getSeller();
        List<AdminOrderBreakdownItemResponse> items = sellerSettlements.stream()
                .map(this::toBreakdownItem)
                .toList();

        return AdminOrderSellerBreakdownResponse.builder()
                .sellerId(seller.getId())
                .sellerBusinessName(seller.getBusinessName())
                .sellerOwnerName(seller.getOwnerName())
                .platformOwned(Boolean.TRUE.equals(seller.getPlatformOwned()))
                .sellerConfirmed(orderSellerAcknowledgementRepository.existsByOrderIdAndSellerId(orderId, seller.getId()))
                .items(items)
                .productTotal(SettlementAggregateUtil.sumGross(sellerSettlements))
                .commissionPercentage(SettlementAggregateUtil.effectiveCommissionPercentage(sellerSettlements))
                .commissionAmount(SettlementAggregateUtil.sumCommission(sellerSettlements))
                .netSellerAmount(SettlementAggregateUtil.sumNet(sellerSettlements))
                .settlementStatus(SettlementAggregateUtil.aggregateStatus(sellerSettlements))
                .build();
    }

    private AdminOrderSellerBreakdownResponse toSellerBreakdownFromOrderItems(Long orderId, Seller seller, List<OrderItem> items) {
        List<AdminOrderBreakdownItemResponse> breakdownItems = items.stream()
                .map(item -> toPlatformBreakdownItem(item, item.getProduct()))
                .toList();
        BigDecimal productTotal = breakdownItems.stream()
                .map(AdminOrderBreakdownItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AdminOrderSellerBreakdownResponse.builder()
                .sellerId(seller.getId())
                .sellerBusinessName(seller.getBusinessName())
                .sellerOwnerName(seller.getOwnerName())
                .platformOwned(Boolean.TRUE.equals(seller.getPlatformOwned()))
                .sellerConfirmed(orderSellerAcknowledgementRepository.existsByOrderIdAndSellerId(orderId, seller.getId()))
                .items(breakdownItems)
                .productTotal(productTotal)
                .commissionPercentage(BigDecimal.ZERO)
                .commissionAmount(BigDecimal.ZERO)
                .netSellerAmount(productTotal)
                .settlementStatus(null)
                .build();
    }

    private AdminOrderBreakdownItemResponse toBreakdownItem(SellerSettlement settlement) {
        OrderItem item = settlement.getOrderItem();
        Product product = item.getProduct();
        return AdminOrderBreakdownItemResponse.builder()
                .settlementId(settlement.getId())
                .orderItemId(item.getId())
                .productId(product != null ? product.getId() : null)
                .productSlug(product != null ? product.getSlug() : null)
                .productName(item.getProductName())
                .productImageUrl(item.getProductImageUrl())
                .qty(item.getQty())
                .price(item.getPrice())
                .lineTotal(settlement.getGrossAmount())
                .commissionPercentage(settlement.getCommissionPercentage())
                .commissionAmount(settlement.getCommissionAmount())
                .netSellerAmount(settlement.getNetSellerAmount())
                .settlementStatus(settlement.getSettlementStatus())
                .build();
    }

    private AdminOrderBreakdownItemResponse toPlatformBreakdownItem(OrderItem item, Product product) {
        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQty()));
        return AdminOrderBreakdownItemResponse.builder()
                .orderItemId(item.getId())
                .productId(product.getId())
                .productSlug(product.getSlug())
                .productName(item.getProductName())
                .productImageUrl(item.getProductImageUrl())
                .qty(item.getQty())
                .price(item.getPrice())
                .lineTotal(lineTotal)
                .commissionPercentage(BigDecimal.ZERO)
                .commissionAmount(BigDecimal.ZERO)
                .netSellerAmount(lineTotal)
                .settlementStatus(null)
                .build();
    }

    private SellerSettlementResponse toResponse(SellerSettlement settlement) {
        OrderItem item = settlement.getOrderItem();
        return SellerSettlementResponse.builder()
                .id(settlement.getId())
                .sellerId(settlement.getSeller().getId())
                .sellerBusinessName(settlement.getSeller().getBusinessName())
                .sellerOwnerName(settlement.getSeller().getOwnerName())
                .orderId(settlement.getOrder().getId())
                .orderNumber(settlement.getOrder().getOrderNumber())
                .orderItemId(item.getId())
                .productName(item.getProductName())
                .grossAmount(settlement.getGrossAmount())
                .commissionPercentage(settlement.getCommissionPercentage())
                .commissionAmount(settlement.getCommissionAmount())
                .netSellerAmount(settlement.getNetSellerAmount())
                .settlementStatus(settlement.getSettlementStatus())
                .settlementDate(settlement.getSettlementDate())
                .transactionReference(settlement.getTransactionReference())
                .matchedFabric(settlement.getMatchedFabric())
                .matchedCategoryName(settlement.getMatchedCategoryName())
                .createdAt(settlement.getCreatedAt())
                .build();
    }
}
