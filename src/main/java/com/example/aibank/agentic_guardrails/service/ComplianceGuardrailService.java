package com.example.aibank.agentic_guardrails.service;

import com.example.aibank.agentic_guardrails.model.ComplianceRule;
import com.example.aibank.agentic_guardrails.repository.ComplianceRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ComplianceGuardrailService {

    private final ComplianceRuleRepository complianceRuleRepository;
    private final ChatClient chatClient;

    public ComplianceGuardrailService(ComplianceRuleRepository complianceRuleRepository,
                                      ChatClient.Builder chatClient) {
        this.complianceRuleRepository = complianceRuleRepository;
        this.chatClient = chatClient.build();
    }

    /**
     * Validate if a user input complies with all active compliance rules
     *
     * @param userInput The user input to validate
     * @return ValidationResult containing compliance status and any violations
     */
    @Transactional(readOnly = true)
    public ValidationResult validateUserInput(String userInput) {
        log.info("Validating user input against compliance rules");
        
        // Get all active compliance rules ordered by priority
        List<ComplianceRule> activeRules = complianceRuleRepository.findAllActiveOrderByPriorityDesc();
        
        if (activeRules.isEmpty()) {
            log.warn("No active compliance rules found");
            return new ValidationResult(true, List.of(), "No active compliance rules to validate against");
        }
        
        // Build the system prompt with all compliance rules
        String rulesText = activeRules.stream()
                .map(rule -> String.format("Rule %s (%s): %s", 
                        rule.getName(), rule.getCategory(), rule.getRuleDefinition()))
                .collect(Collectors.joining("\n\n"));
        
        String systemPrompt = String.format("""
                You are a compliance validation system for a bank. Your task is to check if the user input complies with all the following rules:
                
                %s
                
                Analyze the user input and determine if it violates any of these rules.
                If it complies with all rules, respond with: {"compliant": true}
                If it violates any rules, respond with: {"compliant": false, "violations": ["rule1", "rule2", ...], "explanation": "explanation of violations"}
                
                Only respond with the JSON format specified above, nothing else.
                """, rulesText);
        
        // Create the prompt with system and user messages
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userInput));
        
        // Get the validation result from the AI model
        String response = Objects.requireNonNull(chatClient.prompt(new Prompt(messages)).call().chatResponse())
                .getResult().getOutput().getText();
        
        // Parse the response (in a real implementation, use a proper JSON parser)
        if (response.contains("\"compliant\": true")) {
            return new ValidationResult(true, List.of(), "Input complies with all rules");
        } else {
            // Extract violations and explanation (simplified parsing for demonstration)
            String violations = response.substring(
                    response.indexOf("\"violations\": [") + 15,
                    response.indexOf("]", response.indexOf("\"violations\": ["))
            );
            
            String explanation = response.substring(
                    response.indexOf("\"explanation\": \"") + 16,
                    response.indexOf("\"", response.indexOf("\"explanation\": \"") + 16)
            );
            
            List<String> violationList = List.of(violations.split(","));
            return new ValidationResult(false, violationList, explanation);
        }
    }
    
    /**
     * Get all active compliance rules
     *
     * @return List of active compliance rules
     */
    @Transactional(readOnly = true)
    public List<ComplianceRule> getActiveRules() {
        return complianceRuleRepository.findByActiveTrue();
    }
    
    /**
     * Get compliance rules by category
     *
     * @param category The category to filter by
     * @return List of compliance rules in the specified category
     */
    @Transactional(readOnly = true)
    public List<ComplianceRule> getRulesByCategory(String category) {
        return complianceRuleRepository.findByCategoryAndActiveTrue(category);
    }
    
    /**
     * Create a new compliance rule
     *
     * @param rule The compliance rule to create
     * @return The created compliance rule
     */
    @Transactional
    public ComplianceRule createRule(ComplianceRule rule) {
        log.info("Creating new compliance rule: {}", rule.getName());
        return complianceRuleRepository.save(rule);
    }
    
    /**
     * Update an existing compliance rule
     *
     * @param id The ID of the rule to update
     * @param rule The updated rule data
     * @return The updated compliance rule
     */
    @Transactional
    public ComplianceRule updateRule(String id, ComplianceRule rule) {
        log.info("Updating compliance rule: {}", id);
        
        ComplianceRule existingRule = complianceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compliance rule not found: " + id));
        
        existingRule.setName(rule.getName());
        existingRule.setDescription(rule.getDescription());
        existingRule.setCategory(rule.getCategory());
        existingRule.setRuleDefinition(rule.getRuleDefinition());
        existingRule.setActive(rule.isActive());
        existingRule.setPriority(rule.getPriority());
        
        return complianceRuleRepository.save(existingRule);
    }
    
    /**
     * Delete a compliance rule
     *
     * @param id The ID of the rule to delete
     */
    @Transactional
    public void deleteRule(String id) {
        log.info("Deleting compliance rule: {}", id);
        complianceRuleRepository.deleteById(id);
    }
    
    /**
     * Result of compliance validation
     */
    public static class ValidationResult {
        private final boolean compliant;
        private final List<String> violations;
        private final String explanation;
        
        public ValidationResult(boolean compliant, List<String> violations, String explanation) {
            this.compliant = compliant;
            this.violations = violations;
            this.explanation = explanation;
        }
        
        public boolean isCompliant() {
            return compliant;
        }
        
        public List<String> getViolations() {
            return violations;
        }
        
        public String getExplanation() {
            return explanation;
        }
    }
}
