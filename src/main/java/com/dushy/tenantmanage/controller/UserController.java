package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.PropertyAccessDto;
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
 * Handles user retrieval and property access assignment.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;

    public UserController(UserService userService,
            CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get the currently authenticated user.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    /**
     * Get all users in the system.
     * Typically used by admin/owner for managing access.
     *
     * @return list of all users
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Assign property access to a user.
     * Used to grant assistants access to specific properties.
     *
     * @param accessDto the property access data
     * @return the created PropertyAccess record
     */
    @PostMapping("/access")
    public ResponseEntity<PropertyAccess> assignPropertyAccess(@Valid @RequestBody PropertyAccessDto accessDto) {
        User currentUser = getCurrentUser();
        PropertyAccess access = userService.assignPropertyAccess(
                accessDto.getPropertyId(),
                accessDto.getUserId(),
                accessDto.getAccessLevel(),
                currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(access);
    }
}
