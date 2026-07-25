package com.maahish.repository;

import com.maahish.entity.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommissionRuleRepository extends JpaRepository<CommissionRule, Long> {

    List<CommissionRule> findByEnabledTrueOrderByCreatedAtDesc();

    List<CommissionRule> findAllByOrderByCreatedAtDesc();
}
