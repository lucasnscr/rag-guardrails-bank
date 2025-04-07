package com.example.aibank.agentic_rag.repository;

import com.example.aibank.agentic_rag.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    List<Transaction> findByCustomerId(String customerId);
    
    List<Transaction> findByCustomerIdAndTimestampBetween(String customerId, LocalDateTime start, LocalDateTime end);
    
    List<Transaction> findByAccountId(String accountId);
    
    List<Transaction> findByMerchantCategory(String merchantCategory);
    
    @Query("SELECT t FROM Transaction t WHERE t.customerId = ?1 AND t.amount > ?2 ORDER BY t.timestamp DESC")
    List<Transaction> findLargeTransactionsByCustomer(String customerId, BigDecimal amount);
    
    @Query("SELECT t FROM Transaction t WHERE t.customerId = ?1 AND t.fraudScore > ?2")
    List<Transaction> findPotentialFraudulentTransactions(String customerId, Double fraudScoreThreshold);
    
    @Query("SELECT t FROM Transaction t WHERE t.ipAddress = ?1 AND t.timestamp > ?2")
    List<Transaction> findRecentTransactionsByIpAddress(String ipAddress, LocalDateTime since);
    
    @Query("SELECT t FROM Transaction t WHERE t.deviceId = ?1 AND t.timestamp > ?2")
    List<Transaction> findRecentTransactionsByDeviceId(String deviceId, LocalDateTime since);
}
