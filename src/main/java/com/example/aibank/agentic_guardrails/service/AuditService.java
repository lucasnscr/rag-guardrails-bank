package com.example.aibank.agentic_guardrails.service;

import com.example.aibank.agentic_guardrails.model.AuditLog;
import com.example.aibank.agentic_guardrails.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Log an audit event asynchronously
     *
     * @param userId The ID of the user performing the action
     * @param action The action being performed
     * @param resourceType The type of resource being accessed
     * @param resourceId The ID of the resource being accessed
     * @param requestData The request data (if applicable)
     * @param responseData The response data (if applicable)
     * @param ipAddress The IP address of the user
     * @param userAgent The user agent of the user
     * @param success Whether the action was successful
     * @param errorMessage Error message if the action failed
     */
    @Async
    @Transactional
    public void logEvent(String userId, String action, String resourceType, String resourceId,
                         String requestData, String responseData, String ipAddress, String userAgent,
                         boolean success, String errorMessage) {
        
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(userId)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .requestData(requestData)
                    .responseData(responseData)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .success(success)
                    .errorMessage(errorMessage)
                    .traceId(UUID.randomUUID().toString())
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {}", auditLog.getId());
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Get audit logs for a specific user
     *
     * @param userId The ID of the user
     * @return List of audit logs for the user
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(String userId) {
        return auditLogRepository.findByUserId(userId);
    }

    /**
     * Get recent user activity
     *
     * @param userId The ID of the user
     * @param days Number of days to look back
     * @return List of recent audit logs for the user
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentUserActivity(String userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.findRecentUserActivity(userId, since);
    }

    /**
     * Get recent failed actions
     *
     * @param days Number of days to look back
     * @return List of recent failed audit logs
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentFailedActions(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return auditLogRepository.findRecentFailedActions(since);
    }

    /**
     * Get audit logs for a specific resource
     *
     * @param resourceType The type of resource
     * @param resourceId The ID of the resource
     * @return List of audit logs for the resource
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getResourceAuditLogs(String resourceType, String resourceId) {
        return auditLogRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
    }

    /**
     * Get audit logs for a specific time period
     *
     * @param start The start date and time
     * @param end The end date and time
     * @return List of audit logs for the time period
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    /**
     * Get audit logs by IP address
     *
     * @param ipAddress The IP address to search for
     * @return List of audit logs from the IP address
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByIpAddress(String ipAddress) {
        return auditLogRepository.findByIpAddressOrderByTimestampDesc(ipAddress);
    }
}
