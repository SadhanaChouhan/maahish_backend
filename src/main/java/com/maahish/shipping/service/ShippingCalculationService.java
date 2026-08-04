package com.maahish.shipping.service;

import com.maahish.common.constants.AppConstants;
import com.maahish.common.exception.BadRequestException;
import com.maahish.order.repository.OrderRepository;
import com.maahish.payment.enums.PaymentStatus;
import com.maahish.shipping.dto.response.CheckoutSummaryItemResponse;
import com.maahish.shipping.dto.response.ShippingCalculationResponse;
import com.maahish.shipping.entity.ShippingRule;
import com.maahish.shipping.enums.ShippingRuleType;
import com.maahish.shipping.repository.ShippingRuleRepository;
import com.maahish.catalog.entity.Product;
import com.maahish.catalog.mapper.ProductMapper;
import com.maahish.seller.entity.Seller;
import com.maahish.user.entity.Address;
import com.maahish.user.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ShippingCalculationService {

    private final ShippingRuleRepository shippingRuleRepository;
    private final OrderRepository orderRepository;
    private final ProductMapper productMapper;

    public record CheckoutLine(Product product, int quantity) {}

    @Transactional(readOnly = true)
    public ShippingCalculationResponse calculate(User user, Address address, List<CheckoutLine> lines) {
        List<CheckoutSummaryItemResponse> items = buildSummaryItems(lines);
        BigDecimal subtotal = items.stream()
                .map(CheckoutSummaryItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ShippingRule> rules = shippingRuleRepository.findApplicableRules(LocalDateTime.now());
        if (rules.isEmpty()) {
            throw new BadRequestException("Shipping is not configured. Please contact support.");
        }

        BigDecimal baselineCharge = resolveBaselineCharge(rules);
        boolean firstOrder = orderRepository.countByUserAndPaymentStatus(user, PaymentStatus.COMPLETED) == 0;

        ShippingRule matchedFreeRule = findMatchingFreeRule(rules, subtotal, firstOrder, address);
        if (matchedFreeRule != null) {
            return buildFreeResponse(items, subtotal, baselineCharge, matchedFreeRule, rules, subtotal);
        }

        ShippingRule locationRule = findMatchingLocationRule(rules, address);
        if (locationRule != null) {
            return buildPaidResponse(items, subtotal, locationRule.getShippingCharge(), locationRule, rules, subtotal);
        }

        ShippingRule defaultRule = rules.stream()
                .filter(rule -> rule.getRuleType() == ShippingRuleType.DEFAULT_CHARGE)
                .findFirst()
                .orElse(null);

        return buildPaidResponse(items, subtotal, baselineCharge, defaultRule, rules, subtotal);
    }

    private List<CheckoutSummaryItemResponse> buildSummaryItems(List<CheckoutLine> lines) {
        List<CheckoutSummaryItemResponse> items = new ArrayList<>();
        for (CheckoutLine line : lines) {
            Product product = line.product();
            BigDecimal unitPrice = product.getSellingPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(line.quantity()));
            Seller seller = product.getSeller();

            items.add(CheckoutSummaryItemResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .sellerName(seller != null ? seller.getBusinessName() : AppConstants.BRAND_NAME)
                    .imageUrl(productMapper.toSummary(product).getPrimaryImageUrl())
                    .quantity(line.quantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build());
        }
        return items;
    }

    private BigDecimal resolveBaselineCharge(List<ShippingRule> rules) {
        return rules.stream()
                .filter(rule -> rule.getRuleType() == ShippingRuleType.DEFAULT_CHARGE)
                .findFirst()
                .map(ShippingRule::getShippingCharge)
                .orElse(BigDecimal.ZERO);
    }

    private ShippingRule findMatchingFreeRule(List<ShippingRule> rules, BigDecimal subtotal,
                                              boolean firstOrder, Address address) {
        for (ShippingRule rule : rules) {
            if (!isFreeRuleType(rule.getRuleType())) {
                continue;
            }
            if (matchesFreeRule(rule, subtotal, firstOrder, address)) {
                return rule;
            }
        }
        return null;
    }

    private ShippingRule findMatchingLocationRule(List<ShippingRule> rules, Address address) {
        for (ShippingRule rule : rules) {
            if (rule.getRuleType() == ShippingRuleType.PINCODE_WISE
                    && matchesPincode(rule, address)) {
                return rule;
            }
        }
        for (ShippingRule rule : rules) {
            if (rule.getRuleType() == ShippingRuleType.CITY_WISE
                    && matchesCity(rule, address)) {
                return rule;
            }
        }
        for (ShippingRule rule : rules) {
            if (rule.getRuleType() == ShippingRuleType.STATE_WISE
                    && matchesState(rule, address)) {
                return rule;
            }
        }
        for (ShippingRule rule : rules) {
            if (rule.getRuleType() == ShippingRuleType.EXPRESS_DELIVERY) {
                return rule;
            }
        }
        return null;
    }

    private boolean isFreeRuleType(ShippingRuleType type) {
        return type == ShippingRuleType.FIRST_ORDER_FREE
                || type == ShippingRuleType.MINIMUM_ORDER_FREE
                || type == ShippingRuleType.PROMOTIONAL_FREE;
    }

    private boolean matchesFreeRule(ShippingRule rule, BigDecimal subtotal, boolean firstOrder, Address address) {
        return switch (rule.getRuleType()) {
            case FIRST_ORDER_FREE -> Boolean.TRUE.equals(rule.getIsFirstOrderOnly()) && firstOrder;
            case MINIMUM_ORDER_FREE -> rule.getMinimumOrderAmount() != null
                    && subtotal.compareTo(rule.getMinimumOrderAmount()) >= 0;
            case PROMOTIONAL_FREE -> isWithinSchedule(rule)
                    && (rule.getMinimumOrderAmount() == null
                    || subtotal.compareTo(rule.getMinimumOrderAmount()) >= 0)
                    && matchesOptionalLocation(rule, address);
            default -> false;
        };
    }

    private boolean matchesOptionalLocation(ShippingRule rule, Address address) {
        if (StringUtils.hasText(rule.getPincode()) && !matchesPincode(rule, address)) {
            return false;
        }
        if (StringUtils.hasText(rule.getCity()) && !matchesCity(rule, address)) {
            return false;
        }
        return !StringUtils.hasText(rule.getState()) || matchesState(rule, address);
    }

    private boolean matchesState(ShippingRule rule, Address address) {
        return StringUtils.hasText(rule.getState())
                && StringUtils.hasText(address.getState())
                && rule.getState().trim().equalsIgnoreCase(address.getState().trim());
    }

    private boolean matchesCity(ShippingRule rule, Address address) {
        return StringUtils.hasText(rule.getCity())
                && StringUtils.hasText(address.getCity())
                && rule.getCity().trim().equalsIgnoreCase(address.getCity().trim());
    }

    private boolean matchesPincode(ShippingRule rule, Address address) {
        return StringUtils.hasText(rule.getPincode())
                && StringUtils.hasText(address.getPincode())
                && rule.getPincode().trim().equals(address.getPincode().trim());
    }

    private boolean isWithinSchedule(ShippingRule rule) {
        LocalDateTime now = LocalDateTime.now();
        if (rule.getStartDate() != null && now.isBefore(rule.getStartDate())) {
            return false;
        }
        return rule.getEndDate() == null || !now.isAfter(rule.getEndDate());
    }

    private ShippingCalculationResponse buildFreeResponse(List<CheckoutSummaryItemResponse> items,
                                                          BigDecimal subtotal,
                                                          BigDecimal baselineCharge,
                                                          ShippingRule matchedRule,
                                                          List<ShippingRule> rules,
                                                          BigDecimal currentSubtotal) {
        BigDecimal discount = baselineCharge.max(BigDecimal.ZERO);
        return ShippingCalculationResponse.builder()
                .items(items)
                .subtotal(subtotal)
                .originalShippingCharge(baselineCharge)
                .shippingCharge(BigDecimal.ZERO)
                .shippingDiscount(discount)
                .total(subtotal)
                .freeShipping(true)
                .appliedRuleName(matchedRule.getRuleName())
                .appliedRuleType(matchedRule.getRuleType().name())
                .shippingMessage(buildCongratulationsMessage(matchedRule))
                .upsellMessage(null)
                .build();
    }

    private ShippingCalculationResponse buildPaidResponse(List<CheckoutSummaryItemResponse> items,
                                                            BigDecimal subtotal,
                                                            BigDecimal shippingCharge,
                                                            ShippingRule appliedRule,
                                                            List<ShippingRule> rules,
                                                            BigDecimal currentSubtotal) {
        BigDecimal normalizedShipping = shippingCharge.max(BigDecimal.ZERO);
        return ShippingCalculationResponse.builder()
                .items(items)
                .subtotal(subtotal)
                .originalShippingCharge(normalizedShipping)
                .shippingCharge(normalizedShipping)
                .shippingDiscount(BigDecimal.ZERO)
                .total(subtotal.add(normalizedShipping))
                .freeShipping(normalizedShipping.compareTo(BigDecimal.ZERO) == 0)
                .appliedRuleName(appliedRule != null ? appliedRule.getRuleName() : null)
                .appliedRuleType(appliedRule != null ? appliedRule.getRuleType().name() : null)
                .shippingMessage(null)
                .upsellMessage(buildUpsellMessage(rules, currentSubtotal))
                .build();
    }

    private String buildCongratulationsMessage(ShippingRule rule) {
        return switch (rule.getRuleType()) {
            case FIRST_ORDER_FREE ->
                    "Congratulations! You received FREE delivery on your first order.";
            case MINIMUM_ORDER_FREE ->
                    "Congratulations! Your order qualifies for FREE delivery above "
                            + formatCurrency(rule.getMinimumOrderAmount()) + ".";
            case PROMOTIONAL_FREE ->
                    "Congratulations! Festive free shipping is applied to your order.";
            default -> "Congratulations! FREE shipping is applied to your order.";
        };
    }

    private String buildUpsellMessage(List<ShippingRule> rules, BigDecimal subtotal) {
        return rules.stream()
                .filter(rule -> rule.getRuleType() == ShippingRuleType.MINIMUM_ORDER_FREE)
                .filter(rule -> rule.getMinimumOrderAmount() != null)
                .filter(rule -> subtotal.compareTo(rule.getMinimumOrderAmount()) < 0)
                .findFirst()
                .map(rule -> {
                    BigDecimal remaining = rule.getMinimumOrderAmount().subtract(subtotal)
                            .setScale(0, RoundingMode.CEILING);
                    return "Add " + formatCurrency(remaining) + " more to unlock FREE shipping.";
                })
                .orElse(null);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "₹0";
        }
        return "₹" + amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
