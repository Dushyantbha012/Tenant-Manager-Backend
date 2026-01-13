package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Manages system users (Owners and Assistants).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address.
     * Used for authentication and login.
     *
     * @param email the user's email
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find all users by their user type (OWNER or ASSISTANT).
     *
     * @param userType the type of user to filter by
     * @return list of users matching the type
     */
    List<User> findByUserType(UserType userType);

    /**
     * Check if a user exists with the given email.
     * Used during registration to prevent duplicate accounts.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);
}
