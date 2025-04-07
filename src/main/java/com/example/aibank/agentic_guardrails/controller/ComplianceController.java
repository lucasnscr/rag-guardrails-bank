package com.example.aibank.agentic_guardrails.controller;

import com.example.aibank.agentic_guardrails.model.ComplianceRule;
import com.example.aibank.agentic_guardrails.service.ComplianceGuardrailService;
import com.example.aibank.agentic_guardrails.service.ComplianceGuardrailService.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
@Slf4j
public class ComplianceController {

    private final ComplianceGuardrailService complianceGuardrailService;

    /**
     * Validate user input against compliance rules
     *
     * @param request The validation request containing user input
     * @return Validation result
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidationResult> validateUserInput(@RequestBody ValidationRequest request) {
        log.info("Validating user input against compliance rules");
        ValidationResult result = complianceGuardrailService.validateUserInput(request.getUserInput());
        return ResponseEntity.ok(result);
    }

    /**
     * Get all active compliance rules
     *
     * @return List of active compliance rules
     */
    @GetMapping("/rules/active")
    public ResponseEntity<List<ComplianceRule>> getActiveRules() {
        log.info("Getting all active compliance rules");
        List<ComplianceRule> rules = complianceGuardrailService.getActiveRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Get compliance rules by category
     *
     * @param category The category to filter by
     * @return List of compliance rules in the specified category
     */
    @GetMapping("/rules/category/{category}")
    public ResponseEntity<List<ComplianceRule>> getRulesByCategory(@PathVariable String category) {
        log.info("Getting compliance rules by category: {}", category);
        List<ComplianceRule> rules = complianceGuardrailService.getRulesByCategory(category);
        return ResponseEntity.ok(rules);
    }

    /**
     * Create a new compliance rule
     *
     * @param rule The compliance rule to create
     * @return The created compliance rule
     */
    @PostMapping("/rules")
    public ResponseEntity<ComplianceRule> createRule(@RequestBody ComplianceRule rule) {
        log.info("Creating new compliance rule: {}", rule.getName());
        ComplianceRule createdRule = complianceGuardrailService.createRule(rule);
        return ResponseEntity.ok(createdRule);
    }

    /**
     * Update an existing compliance rule
     *
     * @param id The ID of the rule to update
     * @param rule The updated rule data
     * @return The updated compliance rule
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<ComplianceRule> updateRule(@PathVariable String id, @RequestBody ComplianceRule rule) {
        log.info("Updating compliance rule: {}", id);
        ComplianceRule updatedRule = complianceGuardrailService.updateRule(id, rule);
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * Delete a compliance rule
     *
     * @param id The ID of the rule to delete
     * @return No content response
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable String id) {
        log.info("Deleting compliance rule: {}", id);
        complianceGuardrailService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Request object for validation
     */
    public static class ValidationRequest {
        private String userInput;

        public String getUserInput() {
            return userInput;
        }

        public void setUserInput(String userInput) {
            this.userInput = userInput;
        }
    }
}
