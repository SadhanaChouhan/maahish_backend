package com.maahish.shipping.repository;

import com.maahish.shipping.entity.ShippingRule;
import com.maahish.shipping.enums.ShippingRuleType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {

    @Query("""
            SELECT r FROM ShippingRule r
            WHERE r.active = true
              AND (r.startDate IS NULL OR r.startDate <= :now)
              AND (r.endDate IS NULL OR r.endDate >= :now)
            ORDER BY r.priority ASC, r.id ASC
            """)
    List<ShippingRule> findApplicableRules(@Param("now") LocalDateTime now);

    Optional<ShippingRule> findFirstByRuleTypeAndActiveTrue(ShippingRuleType ruleType);

    Page<ShippingRule> findByRuleNameContainingIgnoreCase(String ruleName, Pageable pageable);

    Page<ShippingRule> findAllByOrderByPriorityAscIdAsc(Pageable pageable);

    boolean existsByRuleType(ShippingRuleType ruleType);
}
