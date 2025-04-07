package com.example.aibank.memory.repository;

import com.example.aibank.memory.model.SessionMemory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionMemoryRepository extends CrudRepository<SessionMemory, String> {
    
    List<SessionMemory> findByUserId(String userId);
    
    List<SessionMemory> findBySessionType(String sessionType);
    
    List<SessionMemory> findByUserIdAndSessionType(String userId, String sessionType);
}
