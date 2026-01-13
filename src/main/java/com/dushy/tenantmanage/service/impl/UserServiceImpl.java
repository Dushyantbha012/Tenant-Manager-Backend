package com.dushy.tenantmanage.service.impl;

import com.dushy.tenantmanage.dto.UserDto;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.PropertyAccess;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.enums.AccessLevel;
import com.dushy.tenantmanage.exception.DuplicateResourceException;
import com.dushy.tenantmanage.exception.InvalidOperationException;
import com.dushy.tenantmanage.exception.ResourceNotFoundException;
import com.dushy.tenantmanage.repository.PropertiesRepository;
import com.dushy.tenantmanage.repository.PropertyAccessRepository;
import com.dushy.tenantmanage.repository.UserRepository;
import com.dushy.tenantmanage.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserService.
 * Handles user registration, authentication, and property access management.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PropertyAccessRepository propertyAccessRepository;
    private final PropertiesRepository propertiesRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
            PropertyAccessRepository propertyAccessRepository,
            PropertiesRepository propertiesRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyAccessRepository = propertyAccessRepository;
        this.propertiesRepository = propertiesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(UserDto userDto) {
        // Check for duplicate email
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("User", "email", userDto.getEmail());
        }

        // Hash password and create user
        User user = User.builder()
                .email(userDto.getEmail())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .fullName(userDto.getFullName())
                .phone(userDto.getPhone())
                .userType(userDto.getUserType())
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.isActive()) {
            throw new InvalidOperationException("User account is inactive");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidOperationException("Invalid credentials");
        }

        return user;
    }

    @Override
    public PropertyAccess assignPropertyAccess(Long propertyId, Long userId, AccessLevel accessLevel,
            Long grantedById) {
        // Validate property exists
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate granter exists
        User grantedBy = userRepository.findById(grantedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", grantedById));

        // Check if access already exists
        Optional<PropertyAccess> existingAccess = propertyAccessRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId);

        if (existingAccess.isPresent()) {
            // Update existing access level
            PropertyAccess access = existingAccess.get();
            access.setAccessLevel(accessLevel);
            return propertyAccessRepository.save(access);
        }

        // Create new property access
        PropertyAccess propertyAccess = PropertyAccess.builder()
                .property(property)
                .user(user)
                .accessLevel(accessLevel)
                .grantedBy(grantedBy)
                .isActive(true)
                .build();

        return propertyAccessRepository.save(propertyAccess);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
