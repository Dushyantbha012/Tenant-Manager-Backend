package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.request.AddAssistantRequest;
import com.dushy.tenantmanage.dto.request.EmailRequest;
import com.dushy.tenantmanage.dto.OwnerDto;
import com.dushy.tenantmanage.dto.PropertyAccessDto;
import com.dushy.tenantmanage.dto.UpdatePasswordDto;
import com.dushy.tenantmanage.dto.UserDto;
import com.dushy.tenantmanage.entity.PropertyAccess;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for user and property access management.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final com.dushy.tenantmanage.security.PropertyAuthorizationService authorizationService;

    public UserController(UserService userService,
            CustomUserDetailsService userDetailsService,
            com.dushy.tenantmanage.security.PropertyAuthorizationService authorizationService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.authorizationService = authorizationService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    // ==================== PROFILE ENDPOINTS ====================

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserProfile() {
        User user = getCurrentUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UserDto userDto) {
        User currentUser = getCurrentUser();
        User updated = userService.updateProfile(currentUser.getId(), userDto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UpdatePasswordDto passwordDto) {
        User currentUser = getCurrentUser();
        userService.changePassword(currentUser.getId(), passwordDto);
        return ResponseEntity.noContent().build();
    }

    // ==================== USER MANAGEMENT ENDPOINTS ====================

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Only allow users to view their own profile
        if (!currentUser.getId().equals(id)) {
            throw new com.dushy.tenantmanage.exception.AccessDeniedException("User", id);
        }
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/assistants")
    public ResponseEntity<List<User>> getAssistants() {
        User currentUser = getCurrentUser();
        List<User> assistants = userService.getAssistants(currentUser.getId());
        return ResponseEntity.ok(assistants);
    }

    @PostMapping("/assistants")
    public ResponseEntity<Void> addAssistant(@RequestBody @Valid EmailRequest request) {
        User currentUser = getCurrentUser();
        userService.addAssistant(currentUser.getId(), request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/assistants/{assistantId}")
    public ResponseEntity<Void> removeAssistant(@PathVariable Long assistantId) {
        User currentUser = getCurrentUser();
        userService.removeAssistant(currentUser.getId(), assistantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get list of owners for whom the current user is an assistant.
     * Used for the mode selector on the frontend.
     */
    @GetMapping("/owners")
    public ResponseEntity<List<OwnerDto>> getOwners() {
        User currentUser = getCurrentUser();
        List<OwnerDto> owners = userService.getOwnersForAssistant(currentUser.getId());
        return ResponseEntity.ok(owners);
    }

    // ==================== ACCESS MANAGEMENT ENDPOINTS ====================

    @PostMapping("/access")
    public ResponseEntity<PropertyAccess> assignPropertyAccess(@Valid @RequestBody PropertyAccessDto accessDto) {
        User currentUser = getCurrentUser();

        // Only property owner can assign access
        authorizationService.checkPropertyOwner(currentUser.getId(), accessDto.getPropertyId());

        PropertyAccess access = userService.assignPropertyAccess(
                accessDto.getPropertyId(),
                accessDto.getUserId(),
                accessDto.getAccessLevel(),
                currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(access);
    }

    @GetMapping("/{userId}/access")
    public ResponseEntity<List<PropertyAccess>> getPropertyAccessByUser(@PathVariable Long userId) {
        User currentUser = getCurrentUser();
        // Only allow users to view their own access
        if (!currentUser.getId().equals(userId)) {
            throw new com.dushy.tenantmanage.exception.AccessDeniedException("User", userId);
        }

        List<PropertyAccess> accessList = userService.getPropertyAccessByUser(userId);
        return ResponseEntity.ok(accessList);
    }

    @DeleteMapping("/access/{accessId}")
    public ResponseEntity<Void> revokeAccess(@PathVariable Long accessId) {
        User currentUser = getCurrentUser();

        // Verify user is owner of the property related to this access
        Long propertyId = authorizationService.getPropertyIdFromAccess(accessId);
        authorizationService.checkPropertyOwner(currentUser.getId(), propertyId);

        userService.revokeAccess(accessId);
        return ResponseEntity.noContent().build();
    }
}
