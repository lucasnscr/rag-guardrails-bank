package com.example.aibank.agentic_rag.advisor;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransactionFraudAdvisor {

    private final VectorStore transactionVectorStore;
    
    private static final String SYSTEM_PROMPT = """
            You are an AI fraud detection expert for a bank. Your task is to analyze a transaction and determine if it might be fraudulent.
            
            Use the following transaction patterns from the bank's database to inform your analysis:
            
            {relevantTransactions}
            
            Consider the following factors when analyzing the transaction:
            1. Transaction amount compared to customer's usual spending
            2. Transaction location compared to customer's usual locations
            3. Transaction time and frequency
            4. Merchant category and previous interactions
            5. Device and IP address used
            
            Provide a fraud score between 0.0 and 1.0, where:
            - 0.0-0.2: Very likely legitimate
            - 0.2-0.4: Probably legitimate
            - 0.4-0.6: Uncertain
            - 0.6-0.8: Suspicious
            - 0.8-1.0: Very likely fraudulent
            
            Also provide a brief explanation for your score and any recommended actions.
            
            Format your response as JSON:
            {
                "fraudScore": <score>,
                "explanation": "<explanation>",
                "recommendedAction": "<action>"
            }
            """;
    
    /**
     * Analyze a transaction for potential fraud
     * 
     * @param transaction The transaction to analyze as a JSON string
     * @return A prompt with the transaction and relevant historical transactions
     */
    public Prompt createFraudAnalysisPrompt(String transaction) {
        // Search for similar transactions in the vector store
        List<Document> relevantTransactions = transactionVectorStore.similaritySearch(
                SearchRequest.builder().defaults()
                        .withQuery(transaction)
                        .withTopK(5)
        );
        
        // Format the relevant transactions for the prompt
        StringBuilder relevantTransactionsText = new StringBuilder();
        for (Document doc : relevantTransactions) {
            relevantTransactionsText.append("- ").append(doc.getText()).append("\n");
        }
        
        // Create the system message with the relevant transactions
        Map<String, Object> model = new HashMap<>();
        model.put("relevantTransactions", relevantTransactionsText.toString());
        
        Message systemMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(model);
        Message userMessage = new UserMessage(transaction);
        
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);
        
        return new Prompt(messages);
    }
}
