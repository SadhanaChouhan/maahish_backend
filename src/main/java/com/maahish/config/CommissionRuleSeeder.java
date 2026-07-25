package com.maahish.config;

import com.maahish.entity.CommissionRule;
import com.maahish.repository.CommissionRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CommissionRuleSeeder {

    private final CommissionRuleRepository commissionRuleRepository;

    @Bean
    CommandLineRunner seedCommissionRules() {
        return args -> {
            if (commissionRuleRepository.count() > 0) {
                return;
            }

            log.info("Seeding default commission rules...");
            seed("Cotton Saree", " Pure Cotton", new BigDecimal("8.00"));
            seed("Cotton Silk Saree", "Cotton Silk", new BigDecimal("10.00"));
            seed("Pure Silk Saree", "Pure Silk", new BigDecimal("12.00"));
        };
    }

    private void seed(String name, String fabric, BigDecimal percentage) {
        commissionRuleRepository.save(CommissionRule.builder()
                .name(name)
                .fabric(fabric)
                .commissionPercentage(percentage)
                .enabled(true)
                .build());
    }
}
