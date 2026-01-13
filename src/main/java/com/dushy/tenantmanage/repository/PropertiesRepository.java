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
     * Find all properties owned by a specific user.
     *
     * @param ownerId the ID of the property owner
     * @return list of properties owned by the user
     */
    List<Properties> findByOwnerId(Long ownerId);

    /**
     * Find all active properties.
     *
     * @return list of active properties
     */
    List<Properties> findByIsActiveTrue();

    /**
     * Find properties by city.
     *
     * @param city the city to search for
     * @return list of properties in the specified city
     */
    List<Properties> findByCity(String city);
}
