package com.example.aibank.agentic_guardrails.controller;

import com.example.aibank.agentic_guardrails.model.Role;
import com.example.aibank.agentic_guardrails.service.RBACService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/rbac")
@RequiredArgsConstructor
@Slf4j
public class RBACController {

    private final RBACService rbacService;

    /**
     * Check if a user has a specific permission
     *
     * @param request The permission check request
     * @return True if the user has the permission, false otherwise
     */
    @PostMapping("/check-permission")
    public ResponseEntity<PermissionCheckResponse> checkPermission(@RequestBody PermissionCheckRequest request) {
        log.info("Checking permission for user: {}", request.getUserId());
        boolean hasPermission = rbacService.hasPermission(
                request.getUserId(),
                request.getRoleName(),
                request.getPermission(),
                request.getIpAddress()
        );
        return ResponseEntity.ok(new PermissionCheckResponse(hasPermission));
    }

    /**
     * Get all permissions for a role
     *
     * @param roleName The name of the role
     * @return Set of permissions for the role
     */
    @GetMapping("/roles/{roleName}/permissions")
    public ResponseEntity<Set<String>> getRolePermissions(@PathVariable String roleName) {
        log.info("Getting permissions for role: {}", roleName);
        Set<String> permissions = rbacService.getRolePermissions(roleName);
        return ResponseEntity.ok(permissions);
    }

    /**
     * Create a new role
     *
     * @param request The role creation request
     * @return The created role
     */
    @PostMapping("/roles")
    public ResponseEntity<Role> createRole(@RequestBody RoleCreationRequest request) {
        log.info("Creating new role: {}", request.getRole().getName());
        Role createdRole = rbacService.createRole(
                request.getRole(),
                request.getUserId(),
                request.getIpAddress()
        );
        return ResponseEntity.ok(createdRole);
    }

    /**
     * Update an existing role
     *
     * @param id The ID of the role to update
     * @param request The role update request
     * @return The updated role
     */
    @PutMapping("/roles/{id}")
    public ResponseEntity<Role> updateRole(
            @PathVariable String id,
            @RequestBody RoleUpdateRequest request) {
        log.info("Updating role: {}", id);
        Role updatedRole = rbacService.updateRole(
                id,
                request.getRole(),
                request.getUserId(),
                request.getIpAddress()
        );
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Delete a role
     *
     * @param id The ID of the role to delete
     * @param request The role deletion request
     * @return No content response
     */
    @DeleteMapping("/roles/{id}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable String id,
            @RequestBody RoleDeletionRequest request) {
        log.info("Deleting role: {}", id);
        rbacService.deleteRole(
                id,
                request.getUserId(),
                request.getIpAddress()
        );
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all roles
     *
     * @return List of all roles
     */
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        log.info("Getting all roles");
        List<Role> roles = rbacService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get a role by ID
     *
     * @param id The ID of the role
     * @return The role
     */
    @GetMapping("/roles/id/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable String id) {
        log.info("Getting role by ID: {}", id);
        Role role = rbacService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Get a role by name
     *
     * @param name The name of the role
     * @return The role
     */
    @GetMapping("/roles/name/{name}")
    public ResponseEntity<Role> getRoleByName(@PathVariable String name) {
        log.info("Getting role by name: {}", name);
        Role role = rbacService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    /**
     * Request object for permission check
     */
    public static class PermissionCheckRequest {
        private String userId;
        private String roleName;
        private String permission;
        private String ipAddress;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public String getPermission() {
            return permission;
        }

        public void setPermission(String permission) {
            this.permission = permission;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }

    /**
     * Response object for permission check
     */
    public static class PermissionCheckResponse {
        private final boolean hasPermission;

        public PermissionCheckResponse(boolean hasPermission) {
            this.hasPermission = hasPermission;
        }

        public boolean isHasPermission() {
            return hasPermission;
        }
    }

    /**
     * Request object for role creation
     */
    public static class RoleCreationRequest {
        private Role role;
        private String userId;
        private String ipAddress;

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }

    /**
     * Request object for role update
     */
    public static class RoleUpdateRequest {
        private Role role;
        private String userId;
        private String ipAddress;

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }

    /**
     * Request object for role deletion
     */
    public static class RoleDeletionRequest {
        private String userId;
        private String ipAddress;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }
}
