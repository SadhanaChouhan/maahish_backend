package com.maahish.config;

import com.maahish.catalog.entity.FabricType;
import com.maahish.catalog.repository.FabricTypeRepository;
import com.maahish.settlement.entity.CommissionRule;
import com.maahish.settlement.repository.CommissionRuleRepository;

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
public class CommissionRuleSeeder {

    private final CommissionRuleRepository commissionRuleRepository;
    private final FabricTypeRepository fabricTypeRepository;

    @Bean
    @Order(2)
    CommandLineRunner seedCommissionRules() {
        return args -> {
            if (commissionRuleRepository.count() > 0) {
                return;
            }

            log.info("Seeding default commission rules...");
            seed("Pure Cotton Saree", "Pure Cotton", new BigDecimal("8.00"));
            seed("Cotton Silk Saree", "Cotton Silk", new BigDecimal("10.00"));
            seed("Pure Silk Saree", "Pure Silk", new BigDecimal("12.00"));
        };
    }

    private void seed(String name, String fabricTypeName, BigDecimal percentage) {
        FabricType fabricType = fabricTypeRepository.findByNameIgnoreCase(fabricTypeName.trim())
                .orElse(null);
        if (fabricType == null) {
            log.warn("Skipping commission rule '{}' — fabric type '{}' not found", name, fabricTypeName);
            return;
        }
        commissionRuleRepository.save(CommissionRule.builder()
                .name(name)
                .fabricType(fabricType)
                .commissionPercentage(percentage)
                .enabled(true)
                .build());
    }
}
