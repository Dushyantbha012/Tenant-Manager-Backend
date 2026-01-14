package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Room entity.
 * Manages individual rentable units.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * Find all rooms on a specific floor.
     *
     * @param floorId the ID of the floor
     * @return list of rooms on the floor
     */
    List<Room> findByFloorId(Long floorId);

    /**
     * Find all available rooms across the system.
     * Rooms that are not occupied and are active.
     *
     * @return list of available rooms
     */
    List<Room> findByIsOccupiedFalseAndIsActiveTrue();

    /**
     * Count total occupied and active rooms.
     * Used for dashboard statistics.
     *
     * @return count of occupied active rooms
     */
    long countByIsOccupiedTrueAndIsActiveTrue();

    /**
     * Find available rooms on a specific floor.
     *
     * @param floorId the ID of the floor
     * @return list of available rooms on the floor
     */
    List<Room> findByFloorIdAndIsOccupiedFalse(Long floorId);

    /**
     * Find all rooms in a property (through floor relationship).
     *
     * @param propertyId the ID of the property
     * @return list of all rooms in the property
     */
    List<Room> findByFloorPropertyId(Long propertyId);

    /**
     * Find vacant rooms in a property.
     *
     * @param propertyId the ID of the property
     * @return list of vacant rooms in the property
     */
    List<Room> findByFloorPropertyIdAndIsOccupiedFalseAndIsActiveTrue(Long propertyId);

    /**
     * Count total rooms in a property.
     *
     * @param propertyId the ID of the property
     * @return total count of rooms
     */
    long countByFloorPropertyId(Long propertyId);

    /**
     * Count occupied rooms in a property.
     *
     * @param propertyId the ID of the property
     * @return count of occupied rooms
     */
    long countByFloorPropertyIdAndIsOccupiedTrue(Long propertyId);

    /**
     * Count all active rooms.
     *
     * @return total count of active rooms
     */
    long countByIsActiveTrue();
}
