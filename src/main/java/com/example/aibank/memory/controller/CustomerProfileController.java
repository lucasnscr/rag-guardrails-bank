package com.example.aibank.memory.controller;

import com.example.aibank.memory.model.CustomerProfile;
import com.example.aibank.memory.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/memory/profile")
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    /**
     * Create a new customer profile
     *
     * @param customerProfile The customer profile to create
     * @return The created customer profile
     */
    @PostMapping
    public ResponseEntity<CustomerProfile> createProfile(@RequestBody CustomerProfile customerProfile) {
        log.info("Creating new customer profile for: {}", customerProfile.getCustomerId());
        CustomerProfile createdProfile = customerProfileService.createProfile(customerProfile);
        return ResponseEntity.ok(createdProfile);
    }

    /**
     * Get a customer profile by ID
     *
     * @param id The ID of the profile
     * @return The customer profile, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerProfile> getProfileById(@PathVariable String id) {
        log.info("Getting customer profile by ID: {}", id);
        Optional<CustomerProfile> profile = customerProfileService.getProfileById(id);
        return profile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a customer profile by customer ID
     *
     * @param customerId The customer ID
     * @return The customer profile, or 404 if not found
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<CustomerProfile> getProfileByCustomerId(@PathVariable String customerId) {
        log.info("Getting customer profile by customer ID: {}", customerId);
        Optional<CustomerProfile> profile = customerProfileService.getProfileByCustomerId(customerId);
        return profile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a customer profile
     *
     * @param id The ID of the profile to update
     * @param customerProfile The updated profile data
     * @return The updated customer profile
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerProfile> updateProfile(
            @PathVariable String id,
            @RequestBody CustomerProfile customerProfile) {
        log.info("Updating customer profile: {}", id);
        CustomerProfile updatedProfile = customerProfileService.updateProfile(id, customerProfile);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Update customer preferences
     *
     * @param customerId The customer ID
     * @param preferences The updated preferences
     * @return The updated customer profile
     */
    @PutMapping("/customer/{customerId}/preferences")
    public ResponseEntity<CustomerProfile> updatePreferences(
            @PathVariable String customerId,
            @RequestBody Map<String, Object> preferences) {
        log.info("Updating preferences for customer: {}", customerId);
        CustomerProfile updatedProfile = customerProfileService.updatePreferences(customerId, preferences);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Update customer behavioral data
     *
     * @param customerId The customer ID
     * @param behavioralData The updated behavioral data
     * @return The updated customer profile
     */
    @PutMapping("/customer/{customerId}/behavioral")
    public ResponseEntity<CustomerProfile> updateBehavioralData(
            @PathVariable String customerId,
            @RequestBody Map<String, Object> behavioralData) {
        log.info("Updating behavioral data for customer: {}", customerId);
        CustomerProfile updatedProfile = customerProfileService.updateBehavioralData(customerId, behavioralData);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Find similar customer profiles
     *
     * @param customerId The customer ID to find similar profiles for
     * @param threshold The similarity threshold (0.0 to 1.0)
     * @param limit The maximum number of results
     * @return List of similar customer profiles
     */
    @GetMapping("/customer/{customerId}/similar")
    public ResponseEntity<List<CustomerProfile>> findSimilarProfiles(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0.7") double threshold,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Finding similar profiles for customer: {}", customerId);
        List<CustomerProfile> similarProfiles = customerProfileService.findSimilarProfiles(customerId, threshold, limit);
        return ResponseEntity.ok(similarProfiles);
    }

    /**
     * Delete a customer profile
     *
     * @param id The ID of the profile to delete
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String id) {
        log.info("Deleting customer profile: {}", id);
        customerProfileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extend the retention period for a customer profile
     *
     * @param customerId The customer ID
     * @param request The retention extension request
     * @return The updated customer profile
     */
    @PostMapping("/customer/{customerId}/retention")
    public ResponseEntity<CustomerProfile> extendRetention(
            @PathVariable String customerId,
            @RequestBody RetentionExtensionRequest request) {
        log.info("Extending retention period for customer: {}", customerId);
        CustomerProfile updatedProfile = customerProfileService.extendRetention(customerId, request.getYears());
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Request object for retention extension
     */
    public static class RetentionExtensionRequest {
        private int years;

        public int getYears() {
            return years;
        }

        public void setYears(int years) {
            this.years = years;
        }
    }
}
