package com.example.aibank.agentic_guardrails.repository;

import com.example.aibank.agentic_guardrails.model.ComplianceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRuleRepository extends JpaRepository<ComplianceRule, String> {
    
    List<ComplianceRule> findByCategory(String category);
    
    List<ComplianceRule> findByActiveTrue();
    
    @Query("SELECT c FROM ComplianceRule c WHERE c.active = true ORDER BY c.priority DESC")
    List<ComplianceRule> findAllActiveOrderByPriorityDesc();
    
    List<ComplianceRule> findByCategoryAndActiveTrue(String category);
}
