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
public class FinancialAdviceAdvisor {

    private final VectorStore financialKnowledgeVectorStore;
    private final ChatModel chatModel;
    private final RetrievalAugmentationAdvisor retrievalAugmentationAdvisor;
    
    private static final String SYSTEM_PROMPT = """
            You are an AI financial advisor for a bank. Your task is to provide personalized financial advice to customers.
            
            Use the following financial knowledge from the bank's database to inform your advice:
            
            {relevantFinancialKnowledge}
            
            Consider the following factors when providing financial advice:
            1. Customer's financial goals and timeline
            2. Customer's risk tolerance
            3. Customer's current financial situation
            4. Market conditions and economic outlook
            5. Tax implications and regulatory considerations
            
            Provide clear, actionable advice that is personalized to the customer's specific situation.
            Your advice should be ethical, compliant with financial regulations, and in the best interest of the customer.
            
            Format your response in a conversational but professional tone, with:
            1. A brief summary of your understanding of the customer's situation
            2. Your specific recommendations with clear reasoning
            3. Any next steps the customer should consider
            """;

    public FinancialAdviceAdvisor(VectorStore financialKnowledgeVectorStore,
                                  ChatModel chatModel) {
        this.financialKnowledgeVectorStore = financialKnowledgeVectorStore;
        this.chatModel = chatModel;
        this.retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(createQueryTransformers())
                .documentRetriever(createDocumentRetriever())
                .queryAugmenter(createQueryAugmenter())
                .build();
    }

    /**
     * Create a financial advice prompt based on customer query and profile
     * 
     * @param customerQuery The customer's financial question
     * @param customerProfile JSON string containing customer profile information
     * @return A prompt with the customer query and relevant financial knowledge
     */
    public Prompt createFinancialAdvicePrompt(String customerQuery, String customerProfile) {
        String combinedQuery = customerQuery + "\n\nCustomer Profile: " + customerProfile;

        AdvisedRequest request = AdvisedRequest.builder()
                .chatModel(chatModel)
                .userText(combinedQuery)
                .build();

        AdvisedRequest advisedRequest = retrievalAugmentationAdvisor.before(request);
        Object contextValue = advisedRequest.adviseContext().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);

        List<Document> relevantKnowledge = new ArrayList<>();
        if (contextValue instanceof List<?>) {
            for (Object obj : (List<?>) contextValue) {
                if (obj instanceof Document doc) {
                    relevantKnowledge.add(doc);
                }
            }
        }

        StringBuilder relevantKnowledgeText = new StringBuilder();
        for (Document doc : relevantKnowledge) {
            relevantKnowledgeText.append("- ").append(doc.getFormattedContent()).append("\n");
        }

        Map<String, Object> model = new HashMap<>();
        model.put("relevantFinancialKnowledge", relevantKnowledgeText.toString());

        Message systemMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(model);
        Message userMessage = new UserMessage(combinedQuery);

        return new Prompt(List.of(systemMessage, userMessage));
    }

    private QueryTransformer createQueryTransformers() {
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .build();
    }

    private VectorStoreDocumentRetriever createDocumentRetriever() {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(financialKnowledgeVectorStore)
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
