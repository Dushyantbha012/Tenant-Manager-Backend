package com.dushy.tenantmanage.service.impl;

import com.dushy.tenantmanage.dto.OwnerDto;
import com.dushy.tenantmanage.dto.UpdatePasswordDto;
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
import com.dushy.tenantmanage.repository.UserAssistantRepository;
import com.dushy.tenantmanage.repository.UserRepository;
import com.dushy.tenantmanage.service.UserService;
import com.dushy.tenantmanage.entity.UserAssistant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final UserAssistantRepository userAssistantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
            PropertyAccessRepository propertyAccessRepository,
            PropertiesRepository propertiesRepository,
            UserAssistantRepository userAssistantRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyAccessRepository = propertyAccessRepository;
        this.propertiesRepository = propertiesRepository;
        this.userAssistantRepository = userAssistantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("User", "email", userDto.getEmail());
        }

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
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        User grantedBy = userRepository.findById(grantedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", grantedById));

        Optional<PropertyAccess> existingAccess = propertyAccessRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId);

        if (existingAccess.isPresent()) {
            PropertyAccess access = existingAccess.get();
            access.setAccessLevel(accessLevel);
            return propertyAccessRepository.save(access);
        }

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

    @Override
    public User updateProfile(Long userId, UserDto userDto) {
        User user = getUserById(userId);
        user.setFullName(userDto.getFullName());
        user.setPhone(userDto.getPhone());
        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long userId, UpdatePasswordDto passwordDto) {
        User user = getUserById(userId);

        if (user.getPasswordHash() != null &&
                !passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAssistants(Long ownerId) {
        return userAssistantRepository.findByOwnerIdAndIsActiveTrue(ownerId).stream()
                .map(UserAssistant::getAssistant)
                .toList();
    }

    public void addAssistant(Long ownerId, String assistantEmail) {
        User owner = getUserById(ownerId);
        User assistant = userRepository.findByEmail(assistantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email: " + assistantEmail));

        if (userAssistantRepository.findByOwnerIdAndAssistantId(ownerId, assistant.getId()).isPresent()) {
            throw new DuplicateResourceException("Assistant", "email", assistantEmail);
        }

        UserAssistant userAssistant = UserAssistant.builder()
                .owner(owner)
                .assistant(assistant)
                .isActive(true)
                .build();
        userAssistantRepository.save(userAssistant);
    }

    public void removeAssistant(Long ownerId, Long assistantId) {
        UserAssistant userAssistant = userAssistantRepository.findByOwnerIdAndAssistantId(ownerId, assistantId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant not found for this owner"));
        userAssistantRepository.delete(userAssistant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyAccess> getPropertyAccessByUser(Long userId) {
        return propertyAccessRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Override
    public void revokeAccess(Long accessId) {
        PropertyAccess access = propertyAccessRepository.findById(accessId)
                .orElseThrow(() -> new ResourceNotFoundException("PropertyAccess", accessId));
        access.setIsActive(false);
        propertyAccessRepository.save(access);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OwnerDto> getOwnersForAssistant(Long userId) {
        List<PropertyAccess> accessList = propertyAccessRepository.findByUserIdAndIsActiveTrue(userId);
        return accessList.stream()
                .map(access -> access.getProperty().getOwner())
                .distinct()
                .map(owner -> OwnerDto.builder()
                        .id(owner.getId())
                        .fullName(owner.getFullName())
                        .email(owner.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}
