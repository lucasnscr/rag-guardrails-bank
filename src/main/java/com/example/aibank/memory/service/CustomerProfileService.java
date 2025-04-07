package com.example.aibank.memory.service;

import com.example.aibank.memory.model.CustomerProfile;
import com.example.aibank.memory.repository.CustomerProfileRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileService {

    private final CustomerProfileRepository customerProfileRepository;
    private final OpenAiEmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    /**
     * Create a new customer profile
     *
     * @param customerProfile The customer profile to create
     * @return The created customer profile
     */
    @Transactional
    public CustomerProfile createProfile(CustomerProfile customerProfile) {
        log.info("Creating new customer profile for: {}", customerProfile.getCustomerId());
        
        // Generate embedding for the customer profile
        String profileText = generateProfileText(customerProfile);
        float[] embedding = embeddingModel.embed(profileText);
        customerProfile.setEmbedding(embedding);
        
        return customerProfileRepository.save(customerProfile);
    }

    /**
     * Get a customer profile by ID
     *
     * @param id The ID of the profile
     * @return The customer profile, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<CustomerProfile> getProfileById(String id) {
        log.debug("Getting customer profile by ID: {}", id);
        return customerProfileRepository.findById(id);
    }

    /**
     * Get a customer profile by customer ID
     *
     * @param customerId The customer ID
     * @return The customer profile, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<CustomerProfile> getProfileByCustomerId(String customerId) {
        log.debug("Getting customer profile by customer ID: {}", customerId);
        return customerProfileRepository.findByCustomerId(customerId);
    }

    /**
     * Update a customer profile
     *
     * @param id The ID of the profile to update
     * @param customerProfile The updated profile data
     * @return The updated customer profile
     */
    @Transactional
    public CustomerProfile updateProfile(String id, CustomerProfile customerProfile) {
        log.info("Updating customer profile: {}", id);
        
        CustomerProfile existingProfile = customerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer profile not found: " + id));
        
        // Update profile fields
        existingProfile.setFirstName(customerProfile.getFirstName());
        existingProfile.setLastName(customerProfile.getLastName());
        existingProfile.setEmail(customerProfile.getEmail());
        existingProfile.setPhoneNumber(customerProfile.getPhoneNumber());
        existingProfile.setPreferences(customerProfile.getPreferences());
        existingProfile.setFinancialData(customerProfile.getFinancialData());
        existingProfile.setBehavioralData(customerProfile.getBehavioralData());
        
        // Regenerate embedding
        String profileText = generateProfileText(existingProfile);
        float[] embedding = embeddingModel.embed(profileText);
        existingProfile.setEmbedding(embedding);
        
        return customerProfileRepository.save(existingProfile);
    }

    /**
     * Update customer preferences
     *
     * @param customerId The customer ID
     * @param preferences The updated preferences
     * @return The updated customer profile
     */
    @Transactional
    public CustomerProfile updatePreferences(String customerId, Map<String, Object> preferences) {
        log.info("Updating preferences for customer: {}", customerId);
        
        CustomerProfile profile = customerProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found: " + customerId));
        
        try {
            String preferencesJson = objectMapper.writeValueAsString(preferences);
            profile.setPreferences(preferencesJson);
            
            // Regenerate embedding
            String profileText = generateProfileText(profile);
            float[] embedding = embeddingModel.embed(profileText);
            profile.setEmbedding(embedding);
            
            return customerProfileRepository.save(profile);
        } catch (JsonProcessingException e) {
            log.error("Error processing preferences JSON", e);
            throw new RuntimeException("Error updating preferences", e);
        }
    }

    /**
     * Update customer behavioral data
     *
     * @param customerId The customer ID
     * @param behavioralData The updated behavioral data
     * @return The updated customer profile
     */
    @Transactional
    public CustomerProfile updateBehavioralData(String customerId, Map<String, Object> behavioralData) {
        log.info("Updating behavioral data for customer: {}", customerId);
        
        CustomerProfile profile = customerProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found: " + customerId));
        
        try {
            String behavioralDataJson = objectMapper.writeValueAsString(behavioralData);
            profile.setBehavioralData(behavioralDataJson);
            
            // Regenerate embedding
            String profileText = generateProfileText(profile);
            float[] embedding = embeddingModel.embed(profileText);
            profile.setEmbedding(embedding);
            
            return customerProfileRepository.save(profile);
        } catch (JsonProcessingException e) {
            log.error("Error processing behavioral data JSON", e);
            throw new RuntimeException("Error updating behavioral data", e);
        }
    }

    /**
     * Find similar customer profiles
     *
     * @param customerId The customer ID to find similar profiles for
     * @param threshold The similarity threshold (0.0 to 1.0)
     * @param limit The maximum number of results
     * @return List of similar customer profiles
     */
    @Transactional(readOnly = true)
    public List<CustomerProfile> findSimilarProfiles(String customerId, double threshold, int limit) {
        log.info("Finding similar profiles for customer: {}", customerId);
        
        CustomerProfile profile = customerProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found: " + customerId));
        
        return customerProfileRepository.findSimilarProfiles(profile.getEmbedding(), threshold, limit);
    }

    /**
     * Delete a customer profile
     *
     * @param id The ID of the profile to delete
     */
    @Transactional
    public void deleteProfile(String id) {
        log.info("Deleting customer profile: {}", id);
        customerProfileRepository.deleteById(id);
    }

    /**
     * Extend the retention period for a customer profile
     *
     * @param customerId The customer ID
     * @param years The number of years to extend the retention period
     * @return The updated customer profile
     */
    @Transactional
    public CustomerProfile extendRetention(String customerId, int years) {
        log.info("Extending retention period for customer: {}", customerId);
        
        CustomerProfile profile = customerProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found: " + customerId));
        
        profile.setRetentionUntil(profile.getRetentionUntil().plusYears(years));
        
        return customerProfileRepository.save(profile);
    }

    /**
     * Scheduled task to clean up expired customer profiles
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    @Transactional
    public void cleanupExpiredProfiles() {
        log.info("Cleaning up expired customer profiles");
        
        List<CustomerProfile> expiredProfiles = customerProfileRepository.findByRetentionUntilBefore(LocalDateTime.now());
        
        log.info("Found {} expired profiles to delete", expiredProfiles.size());
        customerProfileRepository.deleteAll(expiredProfiles);
    }

    /**
     * Generate text representation of a customer profile for embedding
     *
     * @param profile The customer profile
     * @return Text representation of the profile
     */
    private String generateProfileText(CustomerProfile profile) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Customer ID: ").append(profile.getCustomerId()).append(". ");
        
        if (profile.getFirstName() != null) {
            sb.append("First Name: ").append(profile.getFirstName()).append(". ");
        }
        
        if (profile.getLastName() != null) {
            sb.append("Last Name: ").append(profile.getLastName()).append(". ");
        }
        
        if (profile.getEmail() != null) {
            sb.append("Email: ").append(profile.getEmail()).append(". ");
        }
        
        if (profile.getPreferences() != null) {
            sb.append("Preferences: ").append(profile.getPreferences()).append(". ");
        }
        
        if (profile.getFinancialData() != null) {
            sb.append("Financial Data: ").append(profile.getFinancialData()).append(". ");
        }
        
        if (profile.getBehavioralData() != null) {
            sb.append("Behavioral Data: ").append(profile.getBehavioralData()).append(". ");
        }
        
        return sb.toString();
    }
}
