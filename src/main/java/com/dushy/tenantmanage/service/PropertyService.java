package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.FloorDto;
import com.dushy.tenantmanage.dto.PropertyDto;
import com.dushy.tenantmanage.dto.RoomDto;
import com.dushy.tenantmanage.entity.Floor;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.Room;

import java.util.List;

/**
 * Service interface for Property management.
 * Handles property hierarchy: Property -> Floor -> Room.
 */
public interface PropertyService {

    /**
     * Create a new property for an owner.
     *
     * @param propertyDto the property data
     * @param ownerId     the ID of the owner
     * @return the created property
     */
    Properties createProperty(PropertyDto propertyDto, Long ownerId);

    /**
     * Add a floor to a property.
     *
     * @param floorDto   the floor data
     * @param propertyId the ID of the property
     * @return the created floor
     */
    Floor addFloor(FloorDto floorDto, Long propertyId);

    /**
     * Add a room to a floor.
     *
     * @param roomDto the room data
     * @param floorId the ID of the floor
     * @return the created room
     */
    Room addRoom(RoomDto roomDto, Long floorId);

    /**
     * Get all properties owned by a user.
     *
     * @param ownerId the owner's user ID
     * @return list of properties
     */
    List<Properties> getPropertiesByOwner(Long ownerId);

    /**
     * Get a property by ID.
     *
     * @param id the property ID
     * @return the property
     */
    Properties getPropertyById(Long id);

    /**
     * Get all floors for a property.
     *
     * @param propertyId the property ID
     * @return list of floors ordered by floor number
     */
    List<Floor> getFloorsByProperty(Long propertyId);

    /**
     * Get all rooms on a floor.
     *
     * @param floorId the floor ID
     * @return list of rooms
     */
    List<Room> getRoomsByFloor(Long floorId);

    /**
     * Get available (unoccupied) rooms on a floor.
     *
     * @param floorId the floor ID
     * @return list of available rooms
     */
    List<Room> getAvailableRoomsByFloor(Long floorId);
}
