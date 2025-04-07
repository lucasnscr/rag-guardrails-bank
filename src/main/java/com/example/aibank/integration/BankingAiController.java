package com.example.aibank.integration;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Controller for the integrated AI banking solution
 * Provides endpoints for interacting with the AI system with full integration of
 * Agentic RAG, Guardrails, and Memory components
 */
@RestController
@RequestMapping("/api/banking-ai")
@RequiredArgsConstructor
@Slf4j
public class BankingAiController {

    private final BankingAiIntegrationService bankingAiIntegrationService;

    /**
     * Process a user query with full integration of all components
     *
     * @param request The query request
     * @param httpRequest The HTTP request for extracting IP address
     * @return The processing result
     */
    @PostMapping("/query")
    public ResponseEntity<BankingAiIntegrationService.ProcessResult> processQuery(
            @RequestBody QueryRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Received query request from user: {}", request.getUserId());
        
        String ipAddress = httpRequest.getRemoteAddr();
        
        BankingAiIntegrationService.ProcessResult result = bankingAiIntegrationService.processUserQuery(
                request.getUserId(),
                request.getCustomerId(),
                request.getUserRole(),
                request.getQuery(),
                request.getSessionId(),
                ipAddress
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * Request object for user queries
     */
    public static class QueryRequest {
        private String userId;
        private String customerId;
        private String userRole;
        private String query;
        private String sessionId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getCustomerId() {
            return customerId;
        }

        public void setCustomerId(String customerId) {
            this.customerId = customerId;
        }

        public String getUserRole() {
            return userRole;
        }

        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
