package com.example.aibank.memory.service;

import com.example.aibank.memory.model.SessionMemory;
import com.example.aibank.memory.repository.SessionMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionMemoryService {

    private final SessionMemoryRepository sessionMemoryRepository;
    
    @Value("${aibank.session.timeout:30}")
    private Long sessionTimeout;

    /**
     * Create a new session memory
     *
     * @param userId The ID of the user
     * @param sessionType The type of session
     * @return The created session memory
     */
    public SessionMemory createSession(String userId, String sessionType) {
        log.info("Creating new session for user: {}", userId);
        
        SessionMemory sessionMemory = SessionMemory.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .sessionType(sessionType)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .timeToLive(sessionTimeout)
                .build();
        
        return sessionMemoryRepository.save(sessionMemory);
    }

    /**
     * Get a session memory by ID
     *
     * @param sessionId The ID of the session
     * @return The session memory, or empty if not found
     */
    public Optional<SessionMemory> getSession(String sessionId) {
        log.debug("Getting session: {}", sessionId);
        return sessionMemoryRepository.findById(sessionId);
    }

    /**
     * Get all sessions for a user
     *
     * @param userId The ID of the user
     * @return List of session memories for the user
     */
    public List<SessionMemory> getUserSessions(String userId) {
        log.debug("Getting sessions for user: {}", userId);
        return sessionMemoryRepository.findByUserId(userId);
    }

    /**
     * Get all sessions of a specific type for a user
     *
     * @param userId The ID of the user
     * @param sessionType The type of session
     * @return List of session memories of the specified type for the user
     */
    public List<SessionMemory> getUserSessionsByType(String userId, String sessionType) {
        log.debug("Getting {} sessions for user: {}", sessionType, userId);
        return sessionMemoryRepository.findByUserIdAndSessionType(userId, sessionType);
    }

    /**
     * Update a session memory
     *
     * @param sessionMemory The session memory to update
     * @return The updated session memory
     */
    public SessionMemory updateSession(SessionMemory sessionMemory) {
        log.debug("Updating session: {}", sessionMemory.getId());
        sessionMemory.setLastAccessedAt(LocalDateTime.now());
        return sessionMemoryRepository.save(sessionMemory);
    }

    /**
     * Add or update data in a session
     *
     * @param sessionId The ID of the session
     * @param key The key for the data
     * @param value The value of the data
     * @return The updated session memory, or empty if not found
     */
    public Optional<SessionMemory> addSessionData(String sessionId, String key, Object value) {
        log.debug("Adding data to session {}: {}", sessionId, key);
        
        Optional<SessionMemory> sessionOpt = sessionMemoryRepository.findById(sessionId);
        
        if (sessionOpt.isEmpty()) {
            log.warn("Session not found: {}", sessionId);
            return Optional.empty();
        }
        
        SessionMemory session = sessionOpt.get();
        session.addData(key, value);
        session.setLastAccessedAt(LocalDateTime.now());
        
        return Optional.of(sessionMemoryRepository.save(session));
    }

    /**
     * Get data from a session
     *
     * @param sessionId The ID of the session
     * @param key The key for the data
     * @return The value of the data, or empty if not found
     */
    public Optional<Object> getSessionData(String sessionId, String key) {
        log.debug("Getting data from session {}: {}", sessionId, key);
        
        Optional<SessionMemory> sessionOpt = sessionMemoryRepository.findById(sessionId);
        
        if (sessionOpt.isEmpty()) {
            log.warn("Session not found: {}", sessionId);
            return Optional.empty();
        }
        
        SessionMemory session = sessionOpt.get();
        Object data = session.getData(key);
        
        if (data == null) {
            return Optional.empty();
        }
        
        return Optional.of(data);
    }

    /**
     * Remove data from a session
     *
     * @param sessionId The ID of the session
     * @param key The key for the data to remove
     * @return The updated session memory, or empty if not found
     */
    public Optional<SessionMemory> removeSessionData(String sessionId, String key) {
        log.debug("Removing data from session {}: {}", sessionId, key);
        
        Optional<SessionMemory> sessionOpt = sessionMemoryRepository.findById(sessionId);
        
        if (sessionOpt.isEmpty()) {
            log.warn("Session not found: {}", sessionId);
            return Optional.empty();
        }
        
        SessionMemory session = sessionOpt.get();
        session.removeData(key);
        
        return Optional.of(sessionMemoryRepository.save(session));
    }

    /**
     * Delete a session
     *
     * @param sessionId The ID of the session to delete
     */
    public void deleteSession(String sessionId) {
        log.info("Deleting session: {}", sessionId);
        sessionMemoryRepository.deleteById(sessionId);
    }

    /**
     * Delete all sessions for a user
     *
     * @param userId The ID of the user
     */
    public void deleteUserSessions(String userId) {
        log.info("Deleting all sessions for user: {}", userId);
        List<SessionMemory> sessions = sessionMemoryRepository.findByUserId(userId);
        sessionMemoryRepository.deleteAll(sessions);
    }

    /**
     * Scheduled task to clean up expired sessions
     * This is a backup for Redis TTL mechanism
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredSessions() {
        log.info("Cleaning up expired sessions");
        
        Iterable<SessionMemory> allSessions = sessionMemoryRepository.findAll();
        
        for (SessionMemory session : allSessions) {
            if (session.getLastAccessedAt().plusMinutes(session.getTimeToLive()).isBefore(LocalDateTime.now())) {
                log.debug("Deleting expired session: {}", session.getId());
                sessionMemoryRepository.delete(session);
            }
        }
    }
}
