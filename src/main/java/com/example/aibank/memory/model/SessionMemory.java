package com.example.aibank.memory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("session")
public class SessionMemory implements Serializable {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private String sessionType;
    
    private Map<String, Object> data = new HashMap<>();
    
    private LocalDateTime createdAt;
    
    private LocalDateTime lastAccessedAt;
    
    @TimeToLive(unit = TimeUnit.MINUTES)
    private Long timeToLive;
    
    /**
     * Add or update a data item in the session
     * 
     * @param key The key for the data item
     * @param value The value of the data item
     */
    public void addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
        lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * Get a data item from the session
     * 
     * @param key The key for the data item
     * @return The value of the data item, or null if not found
     */
    public Object getData(String key) {
        if (data == null) {
            return null;
        }
        lastAccessedAt = LocalDateTime.now();
        return data.get(key);
    }
    
    /**
     * Remove a data item from the session
     * 
     * @param key The key for the data item to remove
     */
    public void removeData(String key) {
        if (data != null) {
            data.remove(key);
            lastAccessedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Clear all data from the session
     */
    public void clearData() {
        data = new HashMap<>();
        lastAccessedAt = LocalDateTime.now();
    }
}
