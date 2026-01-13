package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.UserDto;
import com.dushy.tenantmanage.entity.PropertyAccess;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.enums.AccessLevel;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User management.
 * Handles authentication, registration, and property access.
 */
public interface UserService {

    /**
     * Register a new user (Owner or Assistant).
     * Password is hashed before storage.
     *
     * @param userDto the user registration data
     * @return the created user
     */
    User registerUser(UserDto userDto);

    /**
     * Authenticate a user by email and password.
     *
     * @param email    the user's email
     * @param password the raw password
     * @return the authenticated user
     */
    User authenticate(String email, String password);

    /**
     * Assign property-specific access to a user (typically an assistant).
     *
     * @param propertyId  the ID of the property
     * @param userId      the ID of the user to grant access
     * @param accessLevel the access level (READ, WRITE, ADMIN)
     * @param grantedById the ID of the user granting access
     * @return the created PropertyAccess record
     */
    PropertyAccess assignPropertyAccess(Long propertyId, Long userId, AccessLevel accessLevel, Long grantedById);

    /**
     * Get a user by ID.
     *
     * @param id the user ID
     * @return the user
     */
    User getUserById(Long id);

    /**
     * Find a user by email.
     *
     * @param email the user's email
     * @return optional containing the user if found
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Get all users in the system.
     *
     * @return list of all users
     */
    List<User> getAllUsers();
}
