package com.example.aibank.agentic_guardrails.controller;

import com.example.aibank.agentic_guardrails.model.AuditLog;
import com.example.aibank.agentic_guardrails.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    /**
     * Get audit logs for a specific user
     *
     * @param userId The ID of the user
     * @return List of audit logs for the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getUserAuditLogs(@PathVariable String userId) {
        log.info("Getting audit logs for user: {}", userId);
        List<AuditLog> auditLogs = auditService.getUserAuditLogs(userId);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get recent user activity
     *
     * @param userId The ID of the user
     * @param days Number of days to look back (default 7)
     * @return List of recent audit logs for the user
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<AuditLog>> getRecentUserActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting recent activity for user: {}", userId);
        List<AuditLog> auditLogs = auditService.getRecentUserActivity(userId, days);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get recent failed actions
     *
     * @param days Number of days to look back (default 7)
     * @return List of recent failed audit logs
     */
    @GetMapping("/failed")
    public ResponseEntity<List<AuditLog>> getRecentFailedActions(
            @RequestParam(defaultValue = "7") int days) {
        log.info("Getting recent failed actions");
        List<AuditLog> auditLogs = auditService.getRecentFailedActions(days);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs for a specific resource
     *
     * @param resourceType The type of resource
     * @param resourceId The ID of the resource
     * @return List of audit logs for the resource
     */
    @GetMapping("/resource/{resourceType}/{resourceId}")
    public ResponseEntity<List<AuditLog>> getResourceAuditLogs(
            @PathVariable String resourceType,
            @PathVariable String resourceId) {
        log.info("Getting audit logs for resource: {}/{}", resourceType, resourceId);
        List<AuditLog> auditLogs = auditService.getResourceAuditLogs(resourceType, resourceId);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs for a specific time period
     *
     * @param start The start date and time
     * @param end The end date and time
     * @return List of audit logs for the time period
     */
    @GetMapping("/timerange")
    public ResponseEntity<List<AuditLog>> getAuditLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.info("Getting audit logs for time range: {} to {}", start, end);
        List<AuditLog> auditLogs = auditService.getAuditLogsByTimeRange(start, end);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs by IP address
     *
     * @param ipAddress The IP address to search for
     * @return List of audit logs from the IP address
     */
    @GetMapping("/ip/{ipAddress}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByIpAddress(@PathVariable String ipAddress) {
        log.info("Getting audit logs for IP address: {}", ipAddress);
        List<AuditLog> auditLogs = auditService.getAuditLogsByIpAddress(ipAddress);
        return ResponseEntity.ok(auditLogs);
    }
}
