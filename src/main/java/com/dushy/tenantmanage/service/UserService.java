package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.UpdatePasswordDto;
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
     */
    User registerUser(UserDto userDto);

    /**
     * Authenticate a user by email and password.
     */
    User authenticate(String email, String password);

    /**
     * Assign property-specific access to a user.
     */
    PropertyAccess assignPropertyAccess(Long propertyId, Long userId, AccessLevel accessLevel, Long grantedById);

    /**
     * Get a user by ID.
     */
    User getUserById(Long id);

    /**
     * Find a user by email.
     */
    Optional<User> getUserByEmail(String email);

    /**
     * Get all users in the system.
     */
    List<User> getAllUsers();

    /**
     * Update user profile.
     *
     * @param userId  the user ID
     * @param userDto the updated profile data
     * @return the updated user
     */
    User updateProfile(Long userId, UserDto userDto);

    /**
     * Change user password.
     *
     * @param userId      the user ID
     * @param passwordDto the password change data
     */
    void changePassword(Long userId, UpdatePasswordDto passwordDto);

    /**
     * Get all assistants (users created by an owner).
     *
     * @param ownerId the owner's user ID
     * @return list of assistant users
     */
    List<User> getAssistants(Long ownerId);

    /**
     * Get property access entries for a user.
     *
     * @param userId the user ID
     * @return list of property access records
     */
    List<PropertyAccess> getPropertyAccessByUser(Long userId);

    /**
     * Revoke property access.
     *
     * @param accessId the access record ID
     */
    void revokeAccess(Long accessId);

    /**
     * Add an assistant to the owner's list.
     *
     * @param ownerId        the owner's ID
     * @param assistantEmail the assistant's email
     */
    void addAssistant(Long ownerId, String assistantEmail);

    /**
     * Remove an assistant from the owner's list.
     *
     * @param ownerId     the owner's ID
     * @param assistantId the assistant's ID
     */
    void removeAssistant(Long ownerId, Long assistantId);
}
