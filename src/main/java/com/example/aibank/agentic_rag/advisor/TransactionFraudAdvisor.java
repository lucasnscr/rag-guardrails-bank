package com.example.aibank.agentic_rag.advisor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TransactionFraudAdvisor {

    private final VectorStore transactionVectorStore;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;
    private final ChatModel chatModel;

    
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

    public TransactionFraudAdvisor(VectorStore transactionVectorStore,
                                   ChatModel chatModel) {
        this.transactionVectorStore = transactionVectorStore;
        this.chatModel = chatModel;
        this.retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(createQueryTransformers())
                .documentRetriever(createDocumentRetriever())
                .queryAugmenter(createQueryAugmenter())
                .build();
    }

    /**
     * Analyze a transaction for potential fraud
     * 
     * @param transaction The transaction to analyze as a JSON string
     * @return A prompt with the transaction and relevant historical transactions
     */
    public Prompt createFraudAnalysisPrompt(String transaction) {

        AdvisedRequest request = AdvisedRequest.builder()
                .chatModel(chatModel)
                .userText(transaction)
                .build();

        AdvisedRequest advisedRequest = retrievalAugmentationAdvisor.before(request);
        Object contextValue = advisedRequest.adviseContext().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);

        List<Document> relevantTransactions = new ArrayList<>();
        if (contextValue instanceof List<?>) {
            for (Object obj : (List<?>) contextValue) {
                if (obj instanceof Document doc) {
                    relevantTransactions.add(doc);
                }
            }
        }
        
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

    private QueryTransformer createQueryTransformers() {
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();
    }

    private VectorStoreDocumentRetriever createDocumentRetriever() {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(transactionVectorStore)
                .similarityThreshold(0.7)
                .topK(5)
                .build();
    }

    private QueryAugmenter createQueryAugmenter() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .build();
    }

}
