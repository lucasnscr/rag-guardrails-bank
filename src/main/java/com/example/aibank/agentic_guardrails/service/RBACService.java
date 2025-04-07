package com.example.aibank.agentic_guardrails.service;

import com.example.aibank.agentic_guardrails.model.Role;
import com.example.aibank.agentic_guardrails.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RBACService {

    private final RoleRepository roleRepository;
    private final AuditService auditService;

    /**
     * Check if a user has a specific permission
     *
     * @param userId The ID of the user
     * @param roleName The name of the role
     * @param permission The permission to check
     * @param ipAddress The IP address of the user
     * @return True if the user has the permission, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String userId, String roleName, String permission, String ipAddress) {
        log.debug("Checking if user {} with role {} has permission {}", userId, roleName, permission);
        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        
        if (roleOpt.isEmpty()) {
            log.warn("Role not found: {}", roleName);
            auditService.logEvent(userId, "PERMISSION_CHECK", "ROLE", roleName,
                    "Permission: " + permission, "Result: Role not found", 
                    ipAddress, null, false, "Role not found");
            return false;
        }
        
        Role role = roleOpt.get();
        boolean hasPermission = role.getPermissions().contains(permission);
        
        auditService.logEvent(userId, "PERMISSION_CHECK", "ROLE", roleName,
                "Permission: " + permission, "Result: " + hasPermission, 
                ipAddress, null, true, null);
        
        return hasPermission;
    }

    /**
     * Get all permissions for a role
     *
     * @param roleName The name of the role
     * @return Set of permissions for the role
     */
    @Transactional(readOnly = true)
    public Set<String> getRolePermissions(String roleName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        
        if (roleOpt.isEmpty()) {
            log.warn("Role not found: {}", roleName);
            throw new RuntimeException("Role not found: " + roleName);
        }
        
        return roleOpt.get().getPermissions();
    }

    /**
     * Create a new role
     *
     * @param role The role to create
     * @param userId The ID of the user creating the role
     * @param ipAddress The IP address of the user
     * @return The created role
     */
    @Transactional
    public Role createRole(Role role, String userId, String ipAddress) {
        log.info("Creating new role: {}", role.getName());
        
        if (roleRepository.existsByName(role.getName())) {
            log.warn("Role already exists: {}", role.getName());
            auditService.logEvent(userId, "CREATE_ROLE", "ROLE", role.getName(),
                    role.toString(), null, ipAddress, null, false, "Role already exists");
            throw new RuntimeException("Role already exists: " + role.getName());
        }
        
        Role savedRole = roleRepository.save(role);
        
        auditService.logEvent(userId, "CREATE_ROLE", "ROLE", savedRole.getId(),
                role.toString(), savedRole.toString(), ipAddress, null, true, null);
        
        return savedRole;
    }

    /**
     * Update an existing role
     *
     * @param id The ID of the role to update
     * @param role The updated role data
     * @param userId The ID of the user updating the role
     * @param ipAddress The IP address of the user
     * @return The updated role
     */
    @Transactional
    public Role updateRole(String id, Role role, String userId, String ipAddress) {
        log.info("Updating role: {}", id);
        
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));
        
        // Store original for audit
        String originalRole = existingRole.toString();
        
        existingRole.setName(role.getName());
        existingRole.setDescription(role.getDescription());
        existingRole.setPermissions(role.getPermissions());
        
        Role updatedRole = roleRepository.save(existingRole);
        
        auditService.logEvent(userId, "UPDATE_ROLE", "ROLE", updatedRole.getId(),
                originalRole, updatedRole.toString(), ipAddress, null, true, null);
        
        return updatedRole;
    }

    /**
     * Delete a role
     *
     * @param id The ID of the role to delete
     * @param userId The ID of the user deleting the role
     * @param ipAddress The IP address of the user
     */
    @Transactional
    public void deleteRole(String id, String userId, String ipAddress) {
        log.info("Deleting role: {}", id);
        
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));
        
        roleRepository.delete(existingRole);
        
        auditService.logEvent(userId, "DELETE_ROLE", "ROLE", id,
                existingRole.toString(), null, ipAddress, null, true, null);
    }

    /**
     * Get all roles
     *
     * @return List of all roles
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Get a role by ID
     *
     * @param id The ID of the role
     * @return The role
     */
    @Transactional(readOnly = true)
    public Role getRoleById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));
    }

    /**
     * Get a role by name
     *
     * @param name The name of the role
     * @return The role
     */
    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found: " + name));
    }
}
