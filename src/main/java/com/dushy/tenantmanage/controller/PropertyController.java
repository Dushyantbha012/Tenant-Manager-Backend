package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.FloorDto;
import com.dushy.tenantmanage.dto.PropertyDto;
import com.dushy.tenantmanage.dto.RoomDto;
import com.dushy.tenantmanage.entity.Floor;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.Room;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for property management.
 * Handles property hierarchy: Property -> Floor -> Room.
 */
@RestController
@RequestMapping("/api")
public class PropertyController {

    private final PropertyService propertyService;
    private final CustomUserDetailsService userDetailsService;

    public PropertyController(PropertyService propertyService,
            CustomUserDetailsService userDetailsService) {
        this.propertyService = propertyService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get the currently authenticated user.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    // ==================== PROPERTY ENDPOINTS ====================

    /**
     * Create a new property for the authenticated owner.
     *
     * @param propertyDto the property data
     * @return the created property
     */
    @PostMapping("/properties")
    public ResponseEntity<Properties> createProperty(@Valid @RequestBody PropertyDto propertyDto) {
        User currentUser = getCurrentUser();
        Properties property = propertyService.createProperty(propertyDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(property);
    }

    /**
     * Get all properties owned by the authenticated user.
     *
     * @return list of properties
     */
    @GetMapping("/properties")
    public ResponseEntity<List<Properties>> getMyProperties() {
        User currentUser = getCurrentUser();
        List<Properties> properties = propertyService.getPropertiesByOwner(currentUser.getId());
        return ResponseEntity.ok(properties);
    }

    /**
     * Get a property by ID.
     *
     * @param id the property ID
     * @return the property
     */
    @GetMapping("/properties/{id}")
    public ResponseEntity<Properties> getPropertyById(@PathVariable Long id) {
        Properties property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    // ==================== FLOOR ENDPOINTS ====================

    /**
     * Get all floors for a property.
     *
     * @param propertyId the property ID
     * @return list of floors
     */
    @GetMapping("/properties/{propertyId}/floors")
    public ResponseEntity<List<Floor>> getFloorsByProperty(@PathVariable Long propertyId) {
        List<Floor> floors = propertyService.getFloorsByProperty(propertyId);
        return ResponseEntity.ok(floors);
    }

    /**
     * Add a floor to a property.
     *
     * @param propertyId the property ID
     * @param floorDto   the floor data
     * @return the created floor
     */
    @PostMapping("/properties/{propertyId}/floors")
    public ResponseEntity<Floor> addFloor(@PathVariable Long propertyId,
            @Valid @RequestBody FloorDto floorDto) {
        Floor floor = propertyService.addFloor(floorDto, propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(floor);
    }

    // ==================== ROOM ENDPOINTS ====================

    /**
     * Get all rooms on a floor.
     *
     * @param floorId the floor ID
     * @return list of rooms
     */
    @GetMapping("/floors/{floorId}/rooms")
    public ResponseEntity<List<Room>> getRoomsByFloor(@PathVariable Long floorId) {
        List<Room> rooms = propertyService.getRoomsByFloor(floorId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Get available (unoccupied) rooms on a floor.
     *
     * @param floorId the floor ID
     * @return list of available rooms
     */
    @GetMapping("/floors/{floorId}/rooms/available")
    public ResponseEntity<List<Room>> getAvailableRooms(@PathVariable Long floorId) {
        List<Room> rooms = propertyService.getAvailableRoomsByFloor(floorId);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Add a room to a floor.
     *
     * @param floorId the floor ID
     * @param roomDto the room data
     * @return the created room
     */
    @PostMapping("/floors/{floorId}/rooms")
    public ResponseEntity<Room> addRoom(@PathVariable Long floorId,
            @Valid @RequestBody RoomDto roomDto) {
        Room room = propertyService.addRoom(roomDto, floorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }
}
