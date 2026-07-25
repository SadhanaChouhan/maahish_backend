package com.maahish.service.impl;

import com.maahish.dto.request.CommissionRuleRequest;
import com.maahish.dto.response.CommissionRuleResponse;
import com.maahish.entity.Category;
import com.maahish.entity.CommissionRule;
import com.maahish.entity.Product;
import com.maahish.exception.BadRequestException;
import com.maahish.exception.ResourceNotFoundException;
import com.maahish.repository.CategoryRepository;
import com.maahish.repository.CommissionRuleRepository;
import com.maahish.service.CommissionRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionRuleServiceImpl implements CommissionRuleService {

    private final CommissionRuleRepository commissionRuleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommissionRuleResponse> getAllRules() {
        return commissionRuleRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionRuleResponse> getEnabledRules() {
        return commissionRuleRepository.findByEnabledTrueOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionRuleResponse getRule(Long id) {
        return toResponse(findRule(id));
    }

    @Override
    @Transactional
    public CommissionRuleResponse createRule(CommissionRuleRequest request) {
        validateRuleRequest(request);
        CommissionRule rule = mapRequest(new CommissionRule(), request);
        return toResponse(commissionRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public CommissionRuleResponse updateRule(Long id, CommissionRuleRequest request) {
        validateRuleRequest(request);
        CommissionRule rule = findRule(id);
        mapRequest(rule, request);
        return toResponse(commissionRuleRepository.save(rule));
    }

    @Override
    @Transactional
    public CommissionRuleResponse setEnabled(Long id, boolean enabled) {
        CommissionRule rule = findRule(id);
        rule.setEnabled(enabled);
        return toResponse(commissionRuleRepository.save(rule));
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionRule resolveRuleForProduct(Product product) {
        List<CommissionRule> enabledRules = commissionRuleRepository.findByEnabledTrueOrderByCreatedAtDesc();
        return enabledRules.stream()
                .map(rule -> new RuleMatch(rule, matchScore(rule, product)))
                .filter(match -> match.score() >= 0)
                .max(Comparator.comparingInt(RuleMatch::score)
                        .thenComparing(match -> match.rule().getCommissionPercentage()))
                .map(RuleMatch::rule)
                .orElse(null);
    }

    private CommissionRule findRule(Long id) {
        return commissionRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission rule not found"));
    }

    private CommissionRule mapRequest(CommissionRule rule, CommissionRuleRequest request) {
        rule.setName(request.getName().trim());
        rule.setFabric(trimToNull(request.getFabric()));
        rule.setCommissionPercentage(request.getCommissionPercentage());
        if (request.getEnabled() != null) {
            rule.setEnabled(request.getEnabled());
        } else if (rule.getEnabled() == null) {
            rule.setEnabled(true);
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            rule.setCategory(category);
        } else {
            rule.setCategory(null);
        }
        return rule;
    }

    private void validateRuleRequest(CommissionRuleRequest request) {
        boolean hasFabric = request.getFabric() != null && !request.getFabric().isBlank();
        boolean hasCategory = request.getCategoryId() != null;
        if (!hasFabric && !hasCategory) {
            throw new BadRequestException("Either fabric or category must be specified for a commission rule");
        }
    }

    private int matchScore(CommissionRule rule, Product product) {
        boolean fabricMatch = rule.getFabric() == null
                || (product.getFabric() != null
                && rule.getFabric().equalsIgnoreCase(product.getFabric().trim()));
        boolean categoryMatch = rule.getCategory() == null
                || (product.getCategory() != null
                && rule.getCategory().getId().equals(product.getCategory().getId()));
        if (!fabricMatch || !categoryMatch) {
            return -1;
        }
        int score = 0;
        if (rule.getFabric() != null) {
            score += 2;
        }
        if (rule.getCategory() != null) {
            score += 1;
        }
        return score;
    }

    private CommissionRuleResponse toResponse(CommissionRule rule) {
        return CommissionRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .fabric(rule.getFabric())
                .categoryId(rule.getCategory() != null ? rule.getCategory().getId() : null)
                .categoryName(rule.getCategory() != null ? rule.getCategory().getName() : null)
                .commissionPercentage(rule.getCommissionPercentage())
                .enabled(rule.getEnabled())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record RuleMatch(CommissionRule rule, int score) {}
}
