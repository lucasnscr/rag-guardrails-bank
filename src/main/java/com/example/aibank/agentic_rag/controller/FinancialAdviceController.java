package com.example.aibank.agentic_rag.controller;

import com.example.aibank.agentic_rag.service.FinancialAdviceService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/financial-advice")
@RequiredArgsConstructor
@Slf4j
public class FinancialAdviceController {

    private final FinancialAdviceService financialAdviceService;

    /**
     * Get personalized financial advice based on customer query and profile
     *
     * @param customerId The customer's ID
     * @param request Request containing the query and customer profile data
     * @return Personalized financial advice
     */
    @PostMapping("/{customerId}")
    public ResponseEntity<FinancialAdviceResponse> getFinancialAdvice(
            @PathVariable String customerId,
            @RequestBody FinancialAdviceRequest request) {
        
        log.info("Generating financial advice for customer: {}", customerId);
        
        String advice = financialAdviceService.getFinancialAdvice(
                request.getQuery(), 
                customerId, 
                request.getCustomerProfile());
        
        FinancialAdviceResponse response = new FinancialAdviceResponse(advice);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Request object for financial advice
     */
    @Setter
    @Getter
    public static class FinancialAdviceRequest {
        // Getters and setters
        private String query;
        private Map<String, Object> customerProfile;

    }
    
    /**
     * Response object for financial advice
     */
    @Getter
    public static class FinancialAdviceResponse {
        // Getters and setters
        private String advice;
        
        public FinancialAdviceResponse(String advice) {
            this.advice = advice;
        }

        public void setAdvice(String advice) {
            this.advice = advice;
        }
    }
}
