package com.dushy.tenantmanage.security;

import com.dushy.tenantmanage.entity.Floor;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.PropertyAccess;
import com.dushy.tenantmanage.entity.Room;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.enums.AccessLevel;
import com.dushy.tenantmanage.exception.AccessDeniedException;
import com.dushy.tenantmanage.exception.ResourceNotFoundException;
import com.dushy.tenantmanage.repository.FloorRepository;
import com.dushy.tenantmanage.repository.PropertiesRepository;
import com.dushy.tenantmanage.repository.PropertyAccessRepository;
import com.dushy.tenantmanage.repository.RoomRepository;
import com.dushy.tenantmanage.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for property-level authorization.
 * Determines if a user can access property resources based on:
 * 1. Ownership - User is the property owner
 * 2. PropertyAccess - User has been granted access with specific level
 */
@Service
@Transactional(readOnly = true)
public class PropertyAuthorizationService {

    private final PropertiesRepository propertiesRepository;
    private final PropertyAccessRepository propertyAccessRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;

    public PropertyAuthorizationService(PropertiesRepository propertiesRepository,
            PropertyAccessRepository propertyAccessRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            TenantRepository tenantRepository) {
        this.propertiesRepository = propertiesRepository;
        this.propertyAccessRepository = propertyAccessRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.tenantRepository = tenantRepository;
    }

    // ==================== PERMISSION CHECK METHODS ====================

    /**
     * Check if user has a specific permission for a property.
     * Owners have all permissions.
     *
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @param permission the required permission
     * @return true if user has permission, false otherwise
     */
    public boolean hasPropertyPermission(Long userId, Long propertyId,
            com.dushy.tenantmanage.enums.PropertyPermission permission) {
        // Check if user is owner
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (property.getOwner().getId().equals(userId)) {
            return true;
        }

        // Check if user has PropertyAccess with the required permission
        Optional<PropertyAccess> access = propertyAccessRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId);

        if (access.isEmpty()) {
            return false;
        }

        // Backward compatibility: ADMIN/WRITE levels imply certain permissions if not
        // explicitly set
        // But for granular control, we primarily check the permissions set.
        // However, if we are transitioning, we might want to map AccessLevel to
        // Permissions.
        // For now, let's assume if permissions set is not empty, we check it.
        // If it is empty, we fallback to AccessLevel (legacy support) or just fail.

        Set<com.dushy.tenantmanage.enums.PropertyPermission> permissions = access.get().getPermissions();
        if (permissions.contains(permission)) {
            return true;
        }

        // Logical mapping for legacy AccessLevel
        AccessLevel level = access.get().getAccessLevel();
        if (level == AccessLevel.ADMIN) {
            return true; // Admin has all permissions
        }
        if (level == AccessLevel.WRITE) {
            // Write implies managing rooms, tenants, payments, settings but maybe not
            // critical things?
            // Let's say WRITE is broad.
            return true;
        }
        if (level == AccessLevel.READ && permission == com.dushy.tenantmanage.enums.PropertyPermission.VIEW_PROPERTY) {
            return true;
        }

        return false;
    }

    /**
     * Check property permission and throw exception if denied.
     *
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @param permission the required permission
     * @throws AccessDeniedException if user has no permission
     */
    public void checkPropertyPermission(Long userId, Long propertyId,
            com.dushy.tenantmanage.enums.PropertyPermission permission) {
        if (!hasPropertyPermission(userId, propertyId, permission)) {
            throw new AccessDeniedException("Property", propertyId, permission.getDescription());
        }
    }

    // ==================== ACCESS CHECK METHODS ====================

    /**
     * Check if user has any access to a property (owner or granted access).
     * 
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @return true if user has access, false otherwise
     */
    public boolean hasPropertyAccess(Long userId, Long propertyId) {
        // Check if user is owner
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (property.getOwner().getId().equals(userId)) {
            return true;
        }

        // Check if user has PropertyAccess
        Optional<PropertyAccess> access = propertyAccessRepository
                .findByPropertyIdAndUserIdAndIsActiveTrue(propertyId, userId);

        return access.isPresent();
    }

    /**
     * Check if user has write access to a property (owner or WRITE/ADMIN access).
     * 
     * @deprecated Use checkPropertyPermission with specific permission instead.
     * 
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @return true if user has write access, false otherwise
     */
    @Deprecated
    public boolean hasPropertyWriteAccess(Long userId, Long propertyId) {
        return hasPropertyPermission(userId, propertyId,
                com.dushy.tenantmanage.enums.PropertyPermission.MANAGE_SETTINGS) || // Assume settings is high level
                hasPropertyPermission(userId, propertyId, com.dushy.tenantmanage.enums.PropertyPermission.MANAGE_ROOMS); // Or
                                                                                                                         // rooms
    }

    /**
     * Check if user is the owner of a property.
     * 
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @return true if user is owner, false otherwise
     */
    public boolean isPropertyOwner(Long userId, Long propertyId) {
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        return property.getOwner().getId().equals(userId);
    }

    // ==================== PROPERTY ID RESOLUTION METHODS ====================

    /**
     * Get property ID from a floor.
     * 
     * @param floorId the floor ID
     * @return the property ID
     */
    public Long getPropertyIdFromFloor(Long floorId) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", floorId));
        return floor.getProperty().getId();
    }

    /**
     * Get property ID from a room.
     * 
     * @param roomId the room ID
     * @return the property ID
     */
    public Long getPropertyIdFromRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        return room.getFloor().getProperty().getId();
    }

    /**
     * Get property ID from a tenant.
     * 
     * @param tenantId the tenant ID
     * @return the property ID
     */
    public Long getPropertyIdFromTenant(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));
        return tenant.getRoom().getFloor().getProperty().getId();
    }

    /**
     * Get property ID from a property access record.
     *
     * @param accessId the property access ID
     * @return the property ID
     */
    public Long getPropertyIdFromAccess(Long accessId) {
        PropertyAccess access = propertyAccessRepository.findById(accessId)
                .orElseThrow(() -> new ResourceNotFoundException("PropertyAccess", accessId));
        return access.getProperty().getId();
    }

    // ==================== THROWING CHECK METHODS ====================

    /**
     * Check property access and throw exception if denied.
     * 
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @throws AccessDeniedException if user has no access
     */
    public void checkPropertyAccess(Long userId, Long propertyId) {
        if (!hasPropertyAccess(userId, propertyId)) {
            throw new AccessDeniedException("Property", propertyId);
        }
    }

    /**
     * Check property write access and throw exception if denied.
     * 
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @throws AccessDeniedException if user has no write access
     */
    public void checkPropertyWriteAccess(Long userId, Long propertyId) {
        if (!hasPropertyWriteAccess(userId, propertyId)) {
            throw new AccessDeniedException("Property", propertyId, "modify");
        }
    }

    /**
     * Check if user is owner and throw exception if not.
     * 
     * @param userId     the user's ID
     * @param propertyId the property's ID
     * @throws AccessDeniedException if user is not owner
     */
    public void checkPropertyOwner(Long userId, Long propertyId) {
        if (!isPropertyOwner(userId, propertyId)) {
            throw new AccessDeniedException("Property", propertyId, "manage");
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get all property IDs that a user has access to (owned + accessed).
     * 
     * @param userId the user's ID
     * @return set of property IDs
     */
    public Set<Long> getAccessiblePropertyIds(Long userId) {
        // Get owned properties
        Set<Long> propertyIds = propertiesRepository.findByOwnerIdOrderByNameAsc(userId)
                .stream()
                .map(Properties::getId)
                .collect(Collectors.toSet());

        // Add properties with granted access
        propertyAccessRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(access -> access.getProperty().getId())
                .forEach(propertyIds::add);

        return propertyIds;
    }

    /**
     * Get all properties a user has access to with their access levels.
     * Useful for determining what operations are allowed.
     * 
     * @param userId the user's ID
     * @return list of properties with owner getting ADMIN level
     */
    public List<Properties> getAccessibleProperties(Long userId) {
        Set<Long> accessibleIds = getAccessiblePropertyIds(userId);
        return propertiesRepository.findByIdInOrderByNameAsc(accessibleIds);
    }
}
