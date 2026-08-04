package com.maahish.config;

import com.maahish.shipping.entity.ShippingRule;
import com.maahish.shipping.enums.ShippingRuleType;
import com.maahish.shipping.repository.ShippingRuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;

@Slf4j
@Configuration
@Profile("local")
@RequiredArgsConstructor
public class ShippingRuleSeeder {

    private final ShippingRuleRepository shippingRuleRepository;

    @Bean
    @Order(3)
    CommandLineRunner seedShippingRules() {
        return args -> {
            seedRule("Default Shipping Charge", ShippingRuleType.DEFAULT_CHARGE,
                    new BigDecimal("99"), null, false, 100);
            seedRule("First Order Free Delivery", ShippingRuleType.FIRST_ORDER_FREE,
                    BigDecimal.ZERO, null, true, 10);
            seedRule("Free Shipping Above Minimum Order", ShippingRuleType.MINIMUM_ORDER_FREE,
                    BigDecimal.ZERO, new BigDecimal("4999"), false, 20);
        };
    }

    private void seedRule(String name, ShippingRuleType type, BigDecimal charge,
                          BigDecimal minimumOrder, boolean firstOrderOnly, int priority) {
        if (shippingRuleRepository.existsByRuleType(type)) {
            return;
        }
        shippingRuleRepository.save(ShippingRule.builder()
                .ruleName(name)
                .ruleType(type)
                .shippingCharge(charge)
                .minimumOrderAmount(minimumOrder)
                .isFirstOrderOnly(firstOrderOnly)
                .priority(priority)
                .active(true)
                .build());
        log.info("Seeded shipping rule: {}", name);
    }
}
