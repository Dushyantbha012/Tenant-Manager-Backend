package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
