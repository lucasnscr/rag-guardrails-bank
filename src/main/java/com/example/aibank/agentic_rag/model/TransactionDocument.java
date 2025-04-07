package com.example.aibank.agentic_rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDocument {
    
    @Id
    private String id;
    
    private String content;
    
    private Map<String, Object> metadata;
    
    private float[] embedding;
    
    private LocalDateTime createdAt;
    
    /**
     * Convert a Transaction to a Document for vector storage
     * 
     * @param transaction The transaction to convert
     * @return A Document representing the transaction
     */
    public static Document toDocument(Transaction transaction) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", transaction.getId());
        metadata.put("accountId", transaction.getAccountId());
        metadata.put("customerId", transaction.getCustomerId());
        metadata.put("amount", transaction.getAmount().toString());
        metadata.put("currency", transaction.getCurrency());
        metadata.put("type", transaction.getType());
        metadata.put("timestamp", transaction.getTimestamp().toString());
        
        if (transaction.getMerchantName() != null) {
            metadata.put("merchantName", transaction.getMerchantName());
        }
        
        if (transaction.getMerchantCategory() != null) {
            metadata.put("merchantCategory", transaction.getMerchantCategory());
        }
        
        if (transaction.getLocation() != null) {
            metadata.put("location", transaction.getLocation());
        }
        
        // Create content string for embedding
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("Transaction ID: ").append(transaction.getId()).append(". ");
        contentBuilder.append("Customer ID: ").append(transaction.getCustomerId()).append(". ");
        contentBuilder.append("Account ID: ").append(transaction.getAccountId()).append(". ");
        contentBuilder.append("Amount: ").append(transaction.getAmount()).append(" ").append(transaction.getCurrency()).append(". ");
        contentBuilder.append("Type: ").append(transaction.getType()).append(". ");
        
        if (transaction.getMerchantName() != null) {
            contentBuilder.append("Merchant: ").append(transaction.getMerchantName()).append(". ");
        }
        
        if (transaction.getMerchantCategory() != null) {
            contentBuilder.append("Category: ").append(transaction.getMerchantCategory()).append(". ");
        }
        
        if (transaction.getDescription() != null) {
            contentBuilder.append("Description: ").append(transaction.getDescription()).append(". ");
        }
        
        if (transaction.getLocation() != null) {
            contentBuilder.append("Location: ").append(transaction.getLocation()).append(". ");
        }
        
        contentBuilder.append("Date: ").append(transaction.getTimestamp()).append(".");
        
        return new Document(transaction.getId(), contentBuilder.toString(), metadata);
    }
}
