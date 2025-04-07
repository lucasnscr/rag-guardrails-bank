package com.example.aibank.memory.repository;

import com.example.aibank.memory.model.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, String> {
    
    Optional<CustomerProfile> findByCustomerId(String customerId);
    
    @Query(value = "SELECT * FROM customer_profiles WHERE embedding <-> cast(:embedding as vector) < :threshold ORDER BY embedding <-> cast(:embedding as vector) LIMIT :limit", nativeQuery = true)
    List<CustomerProfile> findSimilarProfiles(float[] embedding, double threshold, int limit);
    
    List<CustomerProfile> findByRetentionUntilBefore(LocalDateTime date);
    
    @Query("SELECT c FROM CustomerProfile c WHERE c.email = :email")
    Optional<CustomerProfile> findByEmail(String email);
    
    @Query("SELECT c FROM CustomerProfile c WHERE c.phoneNumber = :phoneNumber")
    Optional<CustomerProfile> findByPhoneNumber(String phoneNumber);
}
