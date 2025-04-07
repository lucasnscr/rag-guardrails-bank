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
public class FinancialAdviceAdvisor {

    private final VectorStore financialKnowledgeVectorStore;
    
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
    
    /**
     * Create a financial advice prompt based on customer query and profile
     * 
     * @param customerQuery The customer's financial question
     * @param customerProfile JSON string containing customer profile information
     * @return A prompt with the customer query and relevant financial knowledge
     */
    public Prompt createFinancialAdvicePrompt(String customerQuery, String customerProfile) {
        // Combine query and profile for better context
        String combinedQuery = customerQuery + "\n\nCustomer Profile: " + customerProfile;
        
        // Search for relevant financial knowledge in the vector store
        List<Document> relevantKnowledge = financialKnowledgeVectorStore.similaritySearch(
                SearchRequest.defaults()
                        .withQuery(combinedQuery)
                        .withTopK(5)
        );
        
        // Format the relevant financial knowledge for the prompt
        StringBuilder relevantKnowledgeText = new StringBuilder();
        for (Document doc : relevantKnowledge) {
            relevantKnowledgeText.append("- ").append(doc.getFormattedContent()).append("\n");
        }
        
        // Create the system message with the relevant financial knowledge
        Map<String, Object> model = new HashMap<>();
        model.put("relevantFinancialKnowledge", relevantKnowledgeText.toString());
        
        Message systemMessage = new SystemPromptTemplate(SYSTEM_PROMPT).createMessage(model);
        Message userMessage = new UserMessage(combinedQuery);
        
        List<Message> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);
        
        return new Prompt(messages);
    }
}
