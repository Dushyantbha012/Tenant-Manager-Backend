package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.BulkFloorDto;
import com.dushy.tenantmanage.dto.BulkRoomDto;
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
     * Update a property.
     *
     * @param id          the property ID
     * @param propertyDto the updated data
     * @return the updated property
     */
    Properties updateProperty(Long id, PropertyDto propertyDto);

    /**
     * Delete (soft) a property.
     *
     * @param id the property ID
     */
    void deleteProperty(Long id);

    /**
     * Add a floor to a property.
     *
     * @param floorDto   the floor data
     * @param propertyId the ID of the property
     * @return the created floor
     */
    Floor addFloor(FloorDto floorDto, Long propertyId);

    /**
     * Get a floor by ID.
     *
     * @param id the floor ID
     * @return the floor
     */
    Floor getFloorById(Long id);

    /**
     * Update a floor.
     *
     * @param id       the floor ID
     * @param floorDto the updated data
     * @return the updated floor
     */
    Floor updateFloor(Long id, FloorDto floorDto);

    /**
     * Delete (soft) a floor.
     *
     * @param id the floor ID
     */
    void deleteFloor(Long id);

    /**
     * Add a room to a floor.
     *
     * @param roomDto the room data
     * @param floorId the ID of the floor
     * @return the created room
     */
    Room addRoom(RoomDto roomDto, Long floorId);

    /**
     * Get a room by ID.
     *
     * @param id the room ID
     * @return the room
     */
    Room getRoomById(Long id);

    /**
     * Update a room.
     *
     * @param id      the room ID
     * @param roomDto the updated data
     * @return the updated room
     */
    Room updateRoom(Long id, RoomDto roomDto);

    /**
     * Delete (soft) a room.
     *
     * @param id the room ID
     */
    void deleteRoom(Long id);

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

    /**
     * Get all rooms in a property (flat list).
     *
     * @param propertyId the property ID
     * @return list of all rooms in the property
     */
    List<Room> getRoomsByProperty(Long propertyId);

    /**
     * Get all vacant rooms.
     *
     * @return list of all vacant rooms
     */
    List<Room> getVacantRooms();

    /**
     * Bulk create floors for a property.
     *
     * @param bulkFloorDto the bulk floor data
     * @return list of created floors
     */
    List<Floor> bulkCreateFloors(BulkFloorDto bulkFloorDto);

    /**
     * Bulk create rooms for a floor.
     *
     * @param bulkRoomDto the bulk room data
     * @return list of created rooms
     */
    List<Room> bulkCreateRooms(BulkRoomDto bulkRoomDto);
}
