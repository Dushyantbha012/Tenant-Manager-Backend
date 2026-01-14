package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.BulkFloorDto;
import com.dushy.tenantmanage.dto.BulkRoomDto;
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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    // ==================== PROPERTY ENDPOINTS ====================

    @PostMapping("/properties")
    public ResponseEntity<Properties> createProperty(@Valid @RequestBody PropertyDto propertyDto) {
        User currentUser = getCurrentUser();
        Properties property = propertyService.createProperty(propertyDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(property);
    }

    @GetMapping("/properties")
    public ResponseEntity<List<Properties>> getMyProperties() {
        User currentUser = getCurrentUser();
        List<Properties> properties = propertyService.getPropertiesByOwner(currentUser.getId());
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/properties/{id}")
    public ResponseEntity<Properties> getPropertyById(@PathVariable Long id) {
        Properties property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    @PutMapping("/properties/{id}")
    public ResponseEntity<Properties> updateProperty(@PathVariable Long id,
            @Valid @RequestBody PropertyDto propertyDto) {
        Properties property = propertyService.updateProperty(id, propertyDto);
        return ResponseEntity.ok(property);
    }

    @DeleteMapping("/properties/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/properties/{propertyId}/rooms")
    public ResponseEntity<List<Room>> getRoomsByProperty(@PathVariable Long propertyId) {
        List<Room> rooms = propertyService.getRoomsByProperty(propertyId);
        return ResponseEntity.ok(rooms);
    }

    // ==================== FLOOR ENDPOINTS ====================

    @GetMapping("/properties/{propertyId}/floors")
    public ResponseEntity<List<Floor>> getFloorsByProperty(@PathVariable Long propertyId) {
        List<Floor> floors = propertyService.getFloorsByProperty(propertyId);
        return ResponseEntity.ok(floors);
    }

    @PostMapping("/properties/{propertyId}/floors")
    public ResponseEntity<Floor> addFloor(@PathVariable Long propertyId,
            @Valid @RequestBody FloorDto floorDto) {
        Floor floor = propertyService.addFloor(floorDto, propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(floor);
    }

    @GetMapping("/floors/{floorId}")
    public ResponseEntity<Floor> getFloorById(@PathVariable Long floorId) {
        Floor floor = propertyService.getFloorById(floorId);
        return ResponseEntity.ok(floor);
    }

    @PutMapping("/floors/{id}")
    public ResponseEntity<Floor> updateFloor(@PathVariable Long id,
            @Valid @RequestBody FloorDto floorDto) {
        Floor floor = propertyService.updateFloor(id, floorDto);
        return ResponseEntity.ok(floor);
    }

    @DeleteMapping("/floors/{id}")
    public ResponseEntity<Void> deleteFloor(@PathVariable Long id) {
        propertyService.deleteFloor(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/floors/bulk")
    public ResponseEntity<List<Floor>> bulkCreateFloors(@Valid @RequestBody BulkFloorDto bulkFloorDto) {
        List<Floor> floors = propertyService.bulkCreateFloors(bulkFloorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(floors);
    }

    // ==================== ROOM ENDPOINTS ====================

    @GetMapping("/floors/{floorId}/rooms")
    public ResponseEntity<List<Room>> getRoomsByFloor(@PathVariable Long floorId) {
        List<Room> rooms = propertyService.getRoomsByFloor(floorId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/floors/{floorId}/rooms/available")
    public ResponseEntity<List<Room>> getAvailableRooms(@PathVariable Long floorId) {
        List<Room> rooms = propertyService.getAvailableRoomsByFloor(floorId);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/floors/{floorId}/rooms")
    public ResponseEntity<Room> addRoom(@PathVariable Long floorId,
            @Valid @RequestBody RoomDto roomDto) {
        Room room = propertyService.addRoom(roomDto, floorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long roomId) {
        Room room = propertyService.getRoomById(roomId);
        return ResponseEntity.ok(room);
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id,
            @Valid @RequestBody RoomDto roomDto) {
        Room room = propertyService.updateRoom(id, roomDto);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        propertyService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/vacant")
    public ResponseEntity<List<Room>> getVacantRooms() {
        List<Room> rooms = propertyService.getVacantRooms();
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/rooms/bulk")
    public ResponseEntity<List<Room>> bulkCreateRooms(@Valid @RequestBody BulkRoomDto bulkRoomDto) {
        List<Room> rooms = propertyService.bulkCreateRooms(bulkRoomDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rooms);
    }
}
