package com.example.aibank.integration;

import com.example.aibank.agentic_guardrails.model.AuditLog;
import com.example.aibank.agentic_guardrails.service.AuditService;
import com.example.aibank.agentic_guardrails.service.ComplianceGuardrailService;
import com.example.aibank.agentic_guardrails.service.RBACService;
import com.example.aibank.memory.model.SessionMemory;
import com.example.aibank.memory.service.CustomerProfileService;
import com.example.aibank.memory.service.SessionMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Integration service that connects the Agentic RAG, Guardrails, and Memory components
 * This service provides methods for secure and compliant AI interactions with proper memory management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BankingAiIntegrationService {

    private final ComplianceGuardrailService complianceGuardrailService;
    private final RBACService rbacService;
    private final AuditService auditService;
    private final SessionMemoryService sessionMemoryService;
    private final CustomerProfileService customerProfileService;

    /**
     * Process a user query with full integration of all components
     *
     * @param userId The ID of the user
     * @param customerId The ID of the customer (if applicable)
     * @param userRole The role of the user
     * @param userQuery The query from the user
     * @param sessionId The ID of the current session (or null to create a new one)
     * @param ipAddress The IP address of the user
     * @return ProcessResult containing the response and session information
     */
    public ProcessResult processUserQuery(
            String userId, 
            String customerId, 
            String userRole, 
            String userQuery, 
            String sessionId, 
            String ipAddress) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Processing user query [traceId={}]: User={}, Role={}", traceId, userId, userRole);
        
        try {
            // Step 1: Check permissions using RBAC
            boolean hasPermission = rbacService.hasPermission(
                    userId, userRole, "AI_QUERY", ipAddress);
            
            if (!hasPermission) {
                log.warn("Permission denied for user {} with role {}", userId, userRole);
                auditService.logEvent(
                        userId, "AI_QUERY_ATTEMPT", "USER", userId,
                        userQuery, "Permission denied", ipAddress, null, false, "Insufficient permissions");
                
                return ProcessResult.builder()
                        .success(false)
                        .response("You do not have permission to use this feature.")
                        .build();
            }
            
            // Step 2: Validate query against compliance rules
            ComplianceGuardrailService.ValidationResult validationResult = 
                    complianceGuardrailService.validateUserInput(userQuery);
            
            if (!validationResult.isCompliant()) {
                log.warn("Compliance violation detected: {}", validationResult.getExplanation());
                auditService.logEvent(
                        userId, "AI_QUERY_COMPLIANCE_VIOLATION", "USER", userId,
                        userQuery, validationResult.getExplanation(), ipAddress, null, false, 
                        "Compliance violation: " + String.join(", ", validationResult.getViolations()));
                
                return ProcessResult.builder()
                        .success(false)
                        .response("Your query violates our compliance policies: " + validationResult.getExplanation())
                        .build();
            }
            
            // Step 3: Get or create session memory
            SessionMemory session;
            if (sessionId != null && !sessionId.isEmpty()) {
                Optional<SessionMemory> existingSession = sessionMemoryService.getSession(sessionId);
                if (existingSession.isEmpty()) {
                    log.warn("Session not found: {}", sessionId);
                    session = sessionMemoryService.createSession(userId, "AI_CONVERSATION");
                } else {
                    session = existingSession.get();
                }
            } else {
                session = sessionMemoryService.createSession(userId, "AI_CONVERSATION");
            }
            
            // Step 4: Add query to session memory
            sessionMemoryService.addSessionData(session.getId(), "last_query", userQuery);
            sessionMemoryService.addSessionData(session.getId(), "query_timestamp", System.currentTimeMillis());
            
            // Step 5: Get customer profile if available
            String customerContext = "";
            if (customerId != null && !customerId.isEmpty()) {
                customerProfileService.getProfileByCustomerId(customerId).ifPresent(profile -> {
                    // In a real implementation, we would use this profile data to enhance the AI response
                    log.info("Retrieved customer profile for {}", customerId);
                });
            }
            
            // Step 6: Process the query with AI (simplified for this example)
            // In a real implementation, this would call the appropriate Agentic RAG service
            // based on the query intent (fraud detection or financial advice)
            String aiResponse = "This is a simulated AI response to: " + userQuery;
            
            // Step 7: Add response to session memory
            sessionMemoryService.addSessionData(session.getId(), "last_response", aiResponse);
            sessionMemoryService.addSessionData(session.getId(), "response_timestamp", System.currentTimeMillis());
            
            // Step 8: Log the successful interaction
            auditService.logEvent(
                    userId, "AI_QUERY_SUCCESS", "USER", userId,
                    userQuery, aiResponse, ipAddress, null, true, null);
            
            return ProcessResult.builder()
                    .success(true)
                    .response(aiResponse)
                    .sessionId(session.getId())
                    .build();
            
        } catch (Exception e) {
            log.error("Error processing user query", e);
            auditService.logEvent(
                    userId, "AI_QUERY_ERROR", "USER", userId,
                    userQuery, null, ipAddress, null, false, e.getMessage());
            
            return ProcessResult.builder()
                    .success(false)
                    .response("An error occurred while processing your query. Please try again later.")
                    .build();
        }
    }
    
    /**
     * Result of processing a user query
     */
    @lombok.Builder
    @lombok.Data
    public static class ProcessResult {
        private boolean success;
        private String response;
        private String sessionId;
        private String errorMessage;
    }
}
