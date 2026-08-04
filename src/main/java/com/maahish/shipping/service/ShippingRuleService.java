package com.maahish.shipping.service;

import com.maahish.common.exception.BadRequestException;
import com.maahish.common.exception.ResourceNotFoundException;
import com.maahish.common.dto.response.PageResponse;
import com.maahish.common.util.PageMapper;
import com.maahish.common.util.PaginationUtil;
import com.maahish.shipping.dto.request.ShippingRuleRequest;
import com.maahish.shipping.dto.response.ShippingRuleResponse;
import com.maahish.shipping.entity.ShippingRule;
import com.maahish.shipping.enums.ShippingRuleType;
import com.maahish.shipping.repository.ShippingRuleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingRuleService {

    private final ShippingRuleRepository shippingRuleRepository;

    @Transactional(readOnly = true)
    public PageResponse<ShippingRuleResponse> listRules(String search, int page, int size) {
        Page<ShippingRule> rules = StringUtils.hasText(search)
                ? shippingRuleRepository.findByRuleNameContainingIgnoreCase(
                search.trim(), PaginationUtil.createPageable(page, size, "priority", "asc"))
                : shippingRuleRepository.findAllByOrderByPriorityAscIdAsc(
                PaginationUtil.createPageable(page, size, "priority", "asc"));
        return PageMapper.toPageResponse(rules, this::toResponse);
    }

    @Transactional(readOnly = true)
    public ShippingRuleResponse getRule(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ShippingRuleResponse> getActiveRules() {
        return shippingRuleRepository.findApplicableRules(java.time.LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ShippingRuleResponse createRule(ShippingRuleRequest request) {
        validateRequest(request, null);
        ShippingRule rule = mapToEntity(new ShippingRule(), request);
        return toResponse(shippingRuleRepository.save(rule));
    }

    @Transactional
    public ShippingRuleResponse updateRule(Long id, ShippingRuleRequest request) {
        ShippingRule rule = getEntity(id);
        validateRequest(request, id);
        mapToEntity(rule, request);
        return toResponse(shippingRuleRepository.save(rule));
    }

    @Transactional
    public ShippingRuleResponse activateRule(Long id) {
        ShippingRule rule = getEntity(id);
        rule.setActive(true);
        return toResponse(shippingRuleRepository.save(rule));
    }

    @Transactional
    public ShippingRuleResponse deactivateRule(Long id) {
        ShippingRule rule = getEntity(id);
        if (rule.getRuleType() == ShippingRuleType.DEFAULT_CHARGE) {
            throw new BadRequestException("Default shipping charge rule cannot be deactivated");
        }
        rule.setActive(false);
        return toResponse(shippingRuleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long id) {
        ShippingRule rule = getEntity(id);
        if (rule.getRuleType() == ShippingRuleType.DEFAULT_CHARGE) {
            throw new BadRequestException("Default shipping charge rule cannot be deleted");
        }
        shippingRuleRepository.delete(rule);
    }

    private void validateRequest(ShippingRuleRequest request, Long existingId) {
        if (request.getShippingCharge().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Shipping charge cannot be negative");
        }
        if (request.getRuleType() == ShippingRuleType.MINIMUM_ORDER_FREE
                && (request.getMinimumOrderAmount() == null
                || request.getMinimumOrderAmount().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BadRequestException("Minimum order amount is required for minimum-order free shipping");
        }
        if (request.getRuleType() == ShippingRuleType.STATE_WISE && !StringUtils.hasText(request.getState())) {
            throw new BadRequestException("State is required for state-wise shipping rule");
        }
        if (request.getRuleType() == ShippingRuleType.CITY_WISE && !StringUtils.hasText(request.getCity())) {
            throw new BadRequestException("City is required for city-wise shipping rule");
        }
        if (request.getRuleType() == ShippingRuleType.PINCODE_WISE && !StringUtils.hasText(request.getPincode())) {
            throw new BadRequestException("Pincode is required for pincode-wise shipping rule");
        }
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }
    }

    private ShippingRule mapToEntity(ShippingRule rule, ShippingRuleRequest request) {
        rule.setRuleName(request.getRuleName().trim());
        rule.setRuleType(request.getRuleType());
        rule.setShippingCharge(request.getShippingCharge());
        rule.setMinimumOrderAmount(request.getMinimumOrderAmount());
        rule.setIsFirstOrderOnly(Boolean.TRUE.equals(request.getIsFirstOrderOnly()));
        rule.setState(trimToNull(request.getState()));
        rule.setCity(trimToNull(request.getCity()));
        rule.setPincode(trimToNull(request.getPincode()));
        rule.setPriority(request.getPriority());
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setActive(Boolean.TRUE.equals(request.getActive()));
        return rule;
    }

    private ShippingRule getEntity(Long id) {
        return shippingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipping rule not found"));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private ShippingRuleResponse toResponse(ShippingRule rule) {
        return ShippingRuleResponse.builder()
                .id(rule.getId())
                .ruleName(rule.getRuleName())
                .ruleType(rule.getRuleType())
                .shippingCharge(rule.getShippingCharge())
                .minimumOrderAmount(rule.getMinimumOrderAmount())
                .isFirstOrderOnly(rule.getIsFirstOrderOnly())
                .state(rule.getState())
                .city(rule.getCity())
                .pincode(rule.getPincode())
                .priority(rule.getPriority())
                .startDate(rule.getStartDate())
                .endDate(rule.getEndDate())
                .active(rule.getActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
