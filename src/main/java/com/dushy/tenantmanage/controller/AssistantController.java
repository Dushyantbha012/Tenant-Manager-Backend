package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.AssistantDto;
import com.dushy.tenantmanage.dto.request.AddAssistantRequest;
import com.dushy.tenantmanage.dto.request.UpdatePermissionsRequest;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.PropertyAccess;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.enums.AccessLevel;
import com.dushy.tenantmanage.exception.ResourceNotFoundException;
import com.dushy.tenantmanage.repository.PropertiesRepository;
import com.dushy.tenantmanage.repository.PropertyAccessRepository;
import com.dushy.tenantmanage.repository.UserRepository;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.security.PropertyAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/properties/{propertyId}/assistants")
public class AssistantController {

    private final PropertyAuthorizationService authorizationService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PropertyAccessRepository propertyAccessRepository;
    private final PropertiesRepository propertiesRepository;

    public AssistantController(PropertyAuthorizationService authorizationService,
            CustomUserDetailsService userDetailsService,
            UserRepository userRepository,
            PropertyAccessRepository propertyAccessRepository,
            PropertiesRepository propertiesRepository) {
        this.authorizationService = authorizationService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.propertyAccessRepository = propertyAccessRepository;
        this.propertiesRepository = propertiesRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<AssistantDto> addAssistant(@PathVariable Long propertyId,
            @Valid @RequestBody AddAssistantRequest request) {
        User currentUser = getCurrentUser();
        // Only owner can add assistants
        authorizationService.checkPropertyOwner(currentUser.getId(), propertyId);

        User assistantUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email: " + request.getEmail()));

        // Check if access already exists
        if (propertyAccessRepository.findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, assistantUser.getId())
                .isPresent()) {
            throw new IllegalArgumentException("User is already an assistant for this property");
        }

        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        PropertyAccess access = PropertyAccess.builder()
                .property(property)
                .user(assistantUser)
                .accessLevel(AccessLevel.WRITE) // Default to WRITE for now, but permissions control everything
                .permissions(request.getPermissions())
                .grantedBy(currentUser)
                .isActive(true)
                .build();

        access = propertyAccessRepository.save(access);

        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(access));
    }

    @GetMapping
    public ResponseEntity<List<AssistantDto>> getAssistants(@PathVariable Long propertyId) {
        User currentUser = getCurrentUser();
        authorizationService.checkPropertyOwner(currentUser.getId(), propertyId);

        List<PropertyAccess> assistants = propertyAccessRepository.findByPropertyIdAndIsActiveTrue(propertyId);
        List<AssistantDto> dtos = assistants.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{userId}")
    @Transactional
    public ResponseEntity<AssistantDto> updatePermissions(@PathVariable Long propertyId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdatePermissionsRequest request) {
        User currentUser = getCurrentUser();
        authorizationService.checkPropertyOwner(currentUser.getId(), propertyId);

        PropertyAccess access = propertyAccessRepository.findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant", userId));

        access.setPermissions(request.getPermissions());
        access = propertyAccessRepository.save(access);

        return ResponseEntity.ok(toDto(access));
    }

    @DeleteMapping("/{userId}")
    @Transactional
    public ResponseEntity<Void> removeAssistant(@PathVariable Long propertyId, @PathVariable Long userId) {
        User currentUser = getCurrentUser();
        authorizationService.checkPropertyOwner(currentUser.getId(), propertyId);

        PropertyAccess access = propertyAccessRepository.findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant", userId));

        access.setIsActive(false);
        access.setRevokedAt(java.time.LocalDateTime.now());
        propertyAccessRepository.save(access);

        return ResponseEntity.noContent().build();
    }

    private AssistantDto toDto(PropertyAccess access) {
        return AssistantDto.builder()
                .userId(access.getUser().getId())
                .email(access.getUser().getEmail())
                .fullName(access.getUser().getFullName())
                .permissions(access.getPermissions())
                .isActive(access.getIsActive())
                .build();
    }
}
