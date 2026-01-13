package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.PropertyAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PropertyAccess entity.
 * Manages assistant permissions for specific properties.
 */
@Repository
public interface PropertyAccessRepository extends JpaRepository<PropertyAccess, Long> {

    /**
     * Find all active properties a user has access to.
     *
     * @param userId the ID of the user
     * @return list of active property access records for the user
     */
    List<PropertyAccess> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find all users with active access to a specific property.
     *
     * @param propertyId the ID of the property
     * @return list of active property access records for the property
     */
    List<PropertyAccess> findByPropertyIdAndIsActiveTrue(Long propertyId);

    /**
     * Check specific access level for a user on a property.
     *
     * @param propertyId the ID of the property
     * @param userId     the ID of the user
     * @return Optional containing the active property access record if found
     */
    Optional<PropertyAccess> findByPropertyIdAndUserIdAndIsActiveTrue(Long propertyId, Long userId);
}
