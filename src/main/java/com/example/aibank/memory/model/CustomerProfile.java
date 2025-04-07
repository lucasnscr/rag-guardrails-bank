package com.example.aibank.memory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String customerId;
    
    private String firstName;
    
    private String lastName;
    
    private String email;
    
    private String phoneNumber;
    
    @Column(columnDefinition = "jsonb")
    private String preferences;
    
    @Column(columnDefinition = "jsonb")
    private String financialData;
    
    @Column(columnDefinition = "jsonb")
    private String behavioralData;
    
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private LocalDateTime retentionUntil;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Default retention period of 5 years
        retentionUntil = LocalDateTime.now().plusYears(5);
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
