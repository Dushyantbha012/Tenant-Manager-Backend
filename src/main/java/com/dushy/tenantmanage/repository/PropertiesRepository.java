package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.Properties;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Properties entity.
 * Manages property-level data.
 */
@Repository
public interface PropertiesRepository extends JpaRepository<Properties, Long> {

    /**
     * Find all properties owned by a specific user, ordered by name.
     *
     * @param ownerId the ID of the property owner
     * @return list of properties owned by the user
     */
    List<Properties> findByOwnerIdOrderByNameAsc(Long ownerId);

    /**
     * Find all active properties, ordered by name.
     *
     * @return list of active properties
     */
    List<Properties> findByIsActiveTrueOrderByNameAsc();

    /**
     * Find properties by city, ordered by name.
     *
     * @param city the city to search for
     * @return list of properties in the specified city
     */
    List<Properties> findByCityOrderByNameAsc(String city);

    /**
     * Find properties by a list of IDs, ordered by name.
     *
     * @param ids the list of property IDs
     * @return list of properties
     */
    List<Properties> findByIdInOrderByNameAsc(java.util.Collection<Long> ids);
}
