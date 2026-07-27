package com.maahish.settlement.service;

import com.maahish.settlement.entity.CommissionRule;
import com.maahish.settlement.dto.request.CommissionRuleRequest;
import com.maahish.settlement.dto.response.CommissionRuleResponse;
import com.maahish.catalog.entity.Product;


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
