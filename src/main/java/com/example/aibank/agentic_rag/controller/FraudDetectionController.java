package com.example.aibank.agentic_rag.controller;

import com.example.aibank.agentic_rag.model.Transaction;
import com.example.aibank.agentic_rag.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    /**
     * Process a transaction and detect potential fraud
     *
     * @param transaction The transaction to process
     * @return The processed transaction with fraud score
     */
    @PostMapping("/process")
    public ResponseEntity<Transaction> processTransaction(@RequestBody Transaction transaction) {
        log.info("Received transaction for processing: {}", transaction.getId());
        Transaction processedTransaction = fraudDetectionService.processTransaction(transaction);
        return ResponseEntity.ok(processedTransaction);
    }

    /**
     * Get recent transactions for a customer
     *
     * @param customerId The customer ID
     * @param days Number of days to look back (default 30)
     * @return List of recent transactions
     */
    @GetMapping("/transactions/{customerId}")
    public ResponseEntity<List<Transaction>> getRecentTransactions(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "30") int days) {
        log.info("Retrieving recent transactions for customer: {}", customerId);
        List<Transaction> transactions = fraudDetectionService.getRecentTransactions(customerId, days);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions flagged for review
     *
     * @return List of flagged transactions
     */
    @GetMapping("/flagged")
    public ResponseEntity<List<Transaction>> getFlaggedTransactions() {
        log.info("Retrieving flagged transactions");
        List<Transaction> flaggedTransactions = fraudDetectionService.getFlaggedTransactions();
        return ResponseEntity.ok(flaggedTransactions);
    }
}
