package com.example.aibank.agentic_guardrails.repository;

import com.example.aibank.agentic_guardrails.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    
    List<AuditLog> findByUserId(String userId);
    
    List<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId);
    
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    List<AuditLog> findByActionAndSuccess(String action, boolean success);
    
    @Query("SELECT a FROM AuditLog a WHERE a.userId = ?1 AND a.timestamp >= ?2 ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentUserActivity(String userId, LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.success = false AND a.timestamp >= ?1 ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentFailedActions(LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = ?1 ORDER BY a.timestamp DESC")
    List<AuditLog> findByIpAddressOrderByTimestampDesc(String ipAddress);
}
