package com.example.aibank.agentic_rag.service;

import com.example.aibank.agentic_rag.advisor.TransactionFraudAdvisor;
import com.example.aibank.agentic_rag.model.Transaction;
import com.example.aibank.agentic_rag.model.TransactionDocument;
import com.example.aibank.agentic_rag.repository.TransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final VectorStore transactionVectorStore;
    private final TransactionFraudAdvisor transactionFraudAdvisor;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public FraudDetectionService(TransactionRepository transactionRepository,
                                 VectorStore transactionVectorStore,
                                 TransactionFraudAdvisor transactionFraudAdvisor,
                                 ChatClient.Builder chatClient,
                                 ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.transactionVectorStore = transactionVectorStore;
        this.transactionFraudAdvisor = transactionFraudAdvisor;
        this.chatClient = chatClient.build();
        this.objectMapper = objectMapper;
    }

    /**
     * Process a new transaction and detect potential fraud
     *
     * @param transaction The transaction to process
     * @return The processed transaction with fraud score
     */
    @Transactional
    @CircuitBreaker(name = "fraudDetection", fallbackMethod = "fallbackProcessTransaction")
    @Retry(name = "fraudDetection")
    public Transaction processTransaction(Transaction transaction) {
        log.info("Processing transaction: {}", transaction.getId());
        
        try {
            // Convert transaction to JSON for the advisor
            String transactionJson = objectMapper.writeValueAsString(transaction);
            
            // Create fraud analysis prompt
            var prompt = transactionFraudAdvisor.createFraudAnalysisPrompt(transactionJson);
            
            // Get fraud analysis from AI model
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            String content = Objects.requireNonNull(response).getResult().getOutput().getText();
            
            // Parse the response
            JsonNode jsonResponse = objectMapper.readTree(content);
            double fraudScore = jsonResponse.get("fraudScore").asDouble();
            String explanation = jsonResponse.get("explanation").asText();
            
            // Update transaction with fraud analysis
            transaction.setFraudScore(fraudScore);
            transaction.setFraudReason(explanation);
            transaction.setFlaggedForReview(fraudScore >= 0.6); // Flag if score is suspicious or higher
            
            // Save the updated transaction
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            // Store transaction in vector store for future reference
            Document document = TransactionDocument.toDocument(savedTransaction);
            transactionVectorStore.add(List.of(document));
            
            return savedTransaction;
        } catch (JsonProcessingException e) {
            log.error("Error processing transaction JSON", e);
            throw new RuntimeException("Error processing transaction", e);
        }
    }
    
    /**
     * Fallback method for transaction processing
     *
     * @param transaction The transaction to process
     * @param exception The exception that triggered the fallback
     * @return The transaction with a default fraud score
     */
    private Transaction fallbackProcessTransaction(Transaction transaction, Exception exception) {
        log.warn("Fallback for transaction processing: {}", exception.getMessage());
        
        // Set a conservative fraud score to flag for manual review
        transaction.setFraudScore(0.7);
        transaction.setFraudReason("Automated fraud detection unavailable. Flagged for manual review.");
        transaction.setFlaggedForReview(true);
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Get recent transactions for a customer
     *
     * @param customerId The customer ID
     * @param days Number of days to look back
     * @return List of recent transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions(String customerId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return transactionRepository.findByCustomerIdAndTimestampBetween(
                customerId, since, LocalDateTime.now());
    }
    
    /**
     * Get transactions flagged for review
     *
     * @return List of flagged transactions
     */
    @Transactional(readOnly = true)
    public List<Transaction> getFlaggedTransactions() {
        return transactionRepository.findAll().stream()
                .filter(Transaction::isFlaggedForReview)
                .toList();
    }
}
