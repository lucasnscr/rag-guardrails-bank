package com.example.aibank.memory.controller;

import com.example.aibank.memory.model.SessionMemory;
import com.example.aibank.memory.service.SessionMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/memory/session")
@RequiredArgsConstructor
@Slf4j
public class SessionMemoryController {

    private final SessionMemoryService sessionMemoryService;

    /**
     * Create a new session
     *
     * @param request The session creation request
     * @return The created session
     */
    @PostMapping
    public ResponseEntity<SessionMemory> createSession(@RequestBody SessionCreateRequest request) {
        log.info("Creating new session for user: {}", request.getUserId());
        SessionMemory session = sessionMemoryService.createSession(request.getUserId(), request.getSessionType());
        return ResponseEntity.ok(session);
    }

    /**
     * Get a session by ID
     *
     * @param sessionId The ID of the session
     * @return The session, or 404 if not found
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionMemory> getSession(@PathVariable String sessionId) {
        log.info("Getting session: {}", sessionId);
        Optional<SessionMemory> session = sessionMemoryService.getSession(sessionId);
        return session.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all sessions for a user
     *
     * @param userId The ID of the user
     * @return List of sessions for the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionMemory>> getUserSessions(@PathVariable String userId) {
        log.info("Getting sessions for user: {}", userId);
        List<SessionMemory> sessions = sessionMemoryService.getUserSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Get all sessions of a specific type for a user
     *
     * @param userId The ID of the user
     * @param sessionType The type of session
     * @return List of sessions of the specified type for the user
     */
    @GetMapping("/user/{userId}/type/{sessionType}")
    public ResponseEntity<List<SessionMemory>> getUserSessionsByType(
            @PathVariable String userId,
            @PathVariable String sessionType) {
        log.info("Getting {} sessions for user: {}", sessionType, userId);
        List<SessionMemory> sessions = sessionMemoryService.getUserSessionsByType(userId, sessionType);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Add data to a session
     *
     * @param sessionId The ID of the session
     * @param request The data addition request
     * @return The updated session, or 404 if not found
     */
    @PostMapping("/{sessionId}/data")
    public ResponseEntity<SessionMemory> addSessionData(
            @PathVariable String sessionId,
            @RequestBody SessionDataRequest request) {
        log.info("Adding data to session {}: {}", sessionId, request.getKey());
        Optional<SessionMemory> session = sessionMemoryService.addSessionData(
                sessionId, request.getKey(), request.getValue());
        return session.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get data from a session
     *
     * @param sessionId The ID of the session
     * @param key The key for the data
     * @return The data value, or 404 if not found
     */
    @GetMapping("/{sessionId}/data/{key}")
    public ResponseEntity<Object> getSessionData(
            @PathVariable String sessionId,
            @PathVariable String key) {
        log.info("Getting data from session {}: {}", sessionId, key);
        Optional<Object> data = sessionMemoryService.getSessionData(sessionId, key);
        return data.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Remove data from a session
     *
     * @param sessionId The ID of the session
     * @param key The key for the data to remove
     * @return The updated session, or 404 if not found
     */
    @DeleteMapping("/{sessionId}/data/{key}")
    public ResponseEntity<SessionMemory> removeSessionData(
            @PathVariable String sessionId,
            @PathVariable String key) {
        log.info("Removing data from session {}: {}", sessionId, key);
        Optional<SessionMemory> session = sessionMemoryService.removeSessionData(sessionId, key);
        return session.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a session
     *
     * @param sessionId The ID of the session to delete
     * @return No content response
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable String sessionId) {
        log.info("Deleting session: {}", sessionId);
        sessionMemoryService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete all sessions for a user
     *
     * @param userId The ID of the user
     * @return No content response
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUserSessions(@PathVariable String userId) {
        log.info("Deleting all sessions for user: {}", userId);
        sessionMemoryService.deleteUserSessions(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Request object for session creation
     */
    public static class SessionCreateRequest {
        private String userId;
        private String sessionType;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getSessionType() {
            return sessionType;
        }

        public void setSessionType(String sessionType) {
            this.sessionType = sessionType;
        }
    }

    /**
     * Request object for session data addition
     */
    public static class SessionDataRequest {
        private String key;
        private Object value;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
