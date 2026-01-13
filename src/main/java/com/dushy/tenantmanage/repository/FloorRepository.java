package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Floor entity.
 * Manages floors within properties.
 */
@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    /**
     * Find all floors for a property, ordered by floor number.
     *
     * @param propertyId the ID of the property
     * @return list of floors ordered by floor number ascending
     */
    List<Floor> findByPropertyIdOrderByFloorNumberAsc(Long propertyId);

    /**
     * Find a specific floor by property and floor number.
     * Provides unique lookup for a floor.
     *
     * @param propertyId  the ID of the property
     * @param floorNumber the floor number
     * @return Optional containing the floor if found
     */
    Optional<Floor> findByPropertyIdAndFloorNumber(Long propertyId, Integer floorNumber);
}
