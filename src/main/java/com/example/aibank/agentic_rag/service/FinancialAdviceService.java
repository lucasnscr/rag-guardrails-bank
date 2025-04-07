package com.example.aibank.agentic_rag.service;

import com.example.aibank.agentic_rag.advisor.FinancialAdviceAdvisor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class FinancialAdviceService {

    private final FinancialAdviceAdvisor financialAdviceAdvisor;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public FinancialAdviceService(FinancialAdviceAdvisor financialAdviceAdvisor,
                                  ChatClient.Builder chatClient, ObjectMapper objectMapper) {
        this.financialAdviceAdvisor = financialAdviceAdvisor;
        this.chatClient = chatClient.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Generate personalized financial advice based on customer query and profile
     *
     * @param customerQuery The customer's financial question
     * @param customerId The customer's ID
     * @param customerProfile Additional customer profile information
     * @return Personalized financial advice
     */
    @CircuitBreaker(name = "financialAdvice", fallbackMethod = "fallbackGetFinancialAdvice")
    @Retry(name = "financialAdvice")
    public String getFinancialAdvice(String customerQuery, String customerId, Map<String, Object> customerProfile) {
        log.info("Generating financial advice for customer: {}", customerId);
        
        try {
            // Add customer ID to profile
            Map<String, Object> enrichedProfile = new HashMap<>(customerProfile);
            enrichedProfile.put("customerId", customerId);
            
            // Convert profile to JSON
            String profileJson = objectMapper.writeValueAsString(enrichedProfile);
            
            // Create financial advice prompt
            var prompt = financialAdviceAdvisor.createFinancialAdvicePrompt(customerQuery, profileJson);
            
            // Get advice from AI model
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            return Objects.requireNonNull(response).getResult().getOutput().getText();
            
        } catch (JsonProcessingException e) {
            log.error("Error processing customer profile JSON", e);
            throw new RuntimeException("Error generating financial advice", e);
        }
    }
    
    /**
     * Fallback method for financial advice generation
     *
     * @param customerQuery The customer's financial question
     * @param customerId The customer's ID
     * @param customerProfile Additional customer profile information
     * @param exception The exception that triggered the fallback
     * @return Generic financial advice
     */
    private String fallbackGetFinancialAdvice(String customerQuery, String customerId, 
                                             Map<String, Object> customerProfile, Exception exception) {
        log.warn("Fallback for financial advice: {}", exception.getMessage());
        
        return "I apologize, but I'm unable to provide personalized financial advice at the moment. " +
               "Please consider scheduling an appointment with one of our financial advisors for " +
               "assistance with your query: \"" + customerQuery + "\".";
    }
}
