package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Tenant entity.
 * Manages tenant profiles and occupancy.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /**
     * Find the current active tenant of a room.
     *
     * @param roomId the ID of the room
     * @return Optional containing the active tenant if found
     */
    Optional<Tenant> findByRoomIdAndIsActiveTrue(Long roomId);

    /**
     * Find all currently active tenants.
     *
     * @return list of active tenants
     */
    List<Tenant> findByIsActiveTrue();

    /**
     * Search for a tenant by phone number.
     *
     * @param phone the phone number to search for
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findByPhone(String phone);

    /**
     * Find all tenants who moved out before a specific date.
     * Used for historical records.
     *
     * @param date the cutoff date
     * @return list of tenants who moved out before the date
     */
    List<Tenant> findAllByMoveOutDateBefore(LocalDate date);

    /**
     * Find all active tenants in a property.
     *
     * @param propertyId the ID of the property
     * @return list of active tenants in the property
     */
    List<Tenant> findByIsActiveTrueAndRoomFloorPropertyId(Long propertyId);

    /**
     * Search tenants by name or phone (case-insensitive).
     *
     * @param name  partial name to search
     * @param phone partial phone to search
     * @return list of matching tenants
     */
    List<Tenant> findByFullNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);

    /**
     * Search active tenants by name or phone within a property.
     *
     * @param name       partial name to search
     * @param phone      partial phone to search
     * @param propertyId the ID of the property
     * @return list of matching active tenants in the property
     */
    @Query("SELECT t FROM Tenant t WHERE t.isActive = true AND t.room.floor.property.id = :propertyId " +
            "AND (LOWER(t.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR t.phone LIKE CONCAT('%', :query, '%'))")
    List<Tenant> searchByPropertyId(@Param("query") String query, @Param("propertyId") Long propertyId);

    /**
     * Count active tenants.
     *
     * @return count of active tenants
     */
    long countByIsActiveTrue();
}
