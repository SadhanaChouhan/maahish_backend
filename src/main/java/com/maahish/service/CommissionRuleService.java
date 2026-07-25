package com.maahish.service;

import com.maahish.dto.request.CommissionRuleRequest;
import com.maahish.dto.response.CommissionRuleResponse;
import com.maahish.entity.CommissionRule;
import com.maahish.entity.Product;

import java.util.List;

public interface CommissionRuleService {

    List<CommissionRuleResponse> getAllRules();

    List<CommissionRuleResponse> getEnabledRules();

    CommissionRuleResponse getRule(Long id);

    CommissionRuleResponse createRule(CommissionRuleRequest request);

    CommissionRuleResponse updateRule(Long id, CommissionRuleRequest request);

    CommissionRuleResponse setEnabled(Long id, boolean enabled);

    CommissionRule resolveRuleForProduct(Product product);
}
