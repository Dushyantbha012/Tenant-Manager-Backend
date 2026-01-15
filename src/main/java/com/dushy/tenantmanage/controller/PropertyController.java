package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.BulkFloorDto;
import com.dushy.tenantmanage.dto.BulkRoomDto;
import com.dushy.tenantmanage.dto.FloorDto;
import com.dushy.tenantmanage.dto.PropertyDto;
import com.dushy.tenantmanage.dto.RoomDto;
import com.dushy.tenantmanage.dto.RoomInfoDto;
import com.dushy.tenantmanage.entity.Floor;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.Room;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.security.PropertyAuthorizationService;
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
 * All endpoints are secured with property-level authorization.
 */
@RestController
@RequestMapping("/api")
public class PropertyController {

    private final PropertyService propertyService;
    private final CustomUserDetailsService userDetailsService;
    private final PropertyAuthorizationService authorizationService;

    public PropertyController(PropertyService propertyService,
            CustomUserDetailsService userDetailsService,
            PropertyAuthorizationService authorizationService) {
        this.propertyService = propertyService;
        this.userDetailsService = userDetailsService;
        this.authorizationService = authorizationService;
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
        // Returns only properties user owns or has access to
        List<Properties> properties = authorizationService.getAccessibleProperties(currentUser.getId());
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/properties/{id}")
    public ResponseEntity<Properties> getPropertyById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Check access before returning property
        authorizationService.checkPropertyAccess(currentUser.getId(), id);
        Properties property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    @PutMapping("/properties/{id}")
    public ResponseEntity<Properties> updateProperty(@PathVariable Long id,
            @Valid @RequestBody PropertyDto propertyDto) {
        User currentUser = getCurrentUser();
        // Only owner can update property
        authorizationService.checkPropertyOwner(currentUser.getId(), id);
        Properties property = propertyService.updateProperty(id, propertyDto);
        return ResponseEntity.ok(property);
    }

    @DeleteMapping("/properties/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Only owner can delete property
        authorizationService.checkPropertyOwner(currentUser.getId(), id);
        propertyService.deleteProperty(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/properties/{propertyId}/rooms")
    public ResponseEntity<List<Room>> getRoomsByProperty(@PathVariable Long propertyId) {
        User currentUser = getCurrentUser();
        // Check access to property
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
        List<Room> rooms = propertyService.getRoomsByProperty(propertyId);
        return ResponseEntity.ok(rooms);
    }

    // ==================== FLOOR ENDPOINTS ====================

    @GetMapping("/properties/{propertyId}/floors")
    public ResponseEntity<List<Floor>> getFloorsByProperty(@PathVariable Long propertyId) {
        User currentUser = getCurrentUser();
        // Check access to property
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
        List<Floor> floors = propertyService.getFloorsByProperty(propertyId);
        return ResponseEntity.ok(floors);
    }

    @PostMapping("/properties/{propertyId}/floors")
    public ResponseEntity<Floor> addFloor(@PathVariable Long propertyId,
            @Valid @RequestBody FloorDto floorDto) {
        User currentUser = getCurrentUser();
        // Require write access to add floors
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);
        Floor floor = propertyService.addFloor(floorDto, propertyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(floor);
    }

    @GetMapping("/floors/{floorId}")
    public ResponseEntity<Floor> getFloorById(@PathVariable Long floorId) {
        User currentUser = getCurrentUser();
        // Check access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromFloor(floorId);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
        Floor floor = propertyService.getFloorById(floorId);
        return ResponseEntity.ok(floor);
    }

    @PutMapping("/floors/{id}")
    public ResponseEntity<Floor> updateFloor(@PathVariable Long id,
            @Valid @RequestBody FloorDto floorDto) {
        User currentUser = getCurrentUser();
        // Require write access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromFloor(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);
        Floor floor = propertyService.updateFloor(id, floorDto);
        return ResponseEntity.ok(floor);
    }

    @DeleteMapping("/floors/{id}")
    public ResponseEntity<Void> deleteFloor(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Require write access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromFloor(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);
        propertyService.deleteFloor(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/floors/bulk")
    public ResponseEntity<List<Floor>> bulkCreateFloors(@Valid @RequestBody BulkFloorDto bulkFloorDto) {
        User currentUser = getCurrentUser();
        // Require write access to property
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), bulkFloorDto.getPropertyId());
        List<Floor> floors = propertyService.bulkCreateFloors(bulkFloorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(floors);
    }

    // ==================== ROOM ENDPOINTS ====================

    @GetMapping("/floors/{floorId}/rooms")
    public ResponseEntity<List<Room>> getRoomsByFloor(@PathVariable Long floorId) {
        User currentUser = getCurrentUser();
        // Check access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromFloor(floorId);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
        List<Room> rooms = propertyService.getRoomsByFloor(floorId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/floors/{floorId}/rooms/info")
    public ResponseEntity<List<RoomInfoDto>> getRoomsInfoByFloor(@PathVariable Long floorId) {
        User currentUser = getCurrentUser();
        // Check access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromFloor(floorId);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
        List<RoomInfoDto> roomsInfo = propertyService.getRoomsInfoByFloor(floorId);
        return ResponseEntity.ok(roomsInfo);
    }

    @GetMapping("/floors/{floorId}/rooms/available")
    public ResponseEntity<List<Room>> getAvailableRooms(@PathVariable Long floorId) {
        User currentUser = getCurrentUser();
        // Check access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromFloor(floorId);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
        List<Room> rooms = propertyService.getAvailableRoomsByFloor(floorId);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/floors/{floorId}/rooms")
    public ResponseEntity<Room> addRoom(@PathVariable Long floorId,
            @Valid @RequestBody RoomDto roomDto) {
        User currentUser = getCurrentUser();
        // Require write access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromFloor(floorId);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);
        Room room = propertyService.addRoom(roomDto, floorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long roomId) {
        User currentUser = getCurrentUser();
        // Check access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromRoom(roomId);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
        Room room = propertyService.getRoomById(roomId);
        return ResponseEntity.ok(room);
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id,
            @Valid @RequestBody RoomDto roomDto) {
        User currentUser = getCurrentUser();
        // Require write access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromRoom(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);
        Room room = propertyService.updateRoom(id, roomDto);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Require write access via property hierarchy
        Long propertyId = authorizationService.getPropertyIdFromRoom(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);
        propertyService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/vacant")
    public ResponseEntity<List<Room>> getVacantRooms() {
        User currentUser = getCurrentUser();
        // Get all vacant rooms, then filter by accessible properties
        List<Room> allVacantRooms = propertyService.getVacantRooms();
        List<Room> accessibleRooms = allVacantRooms.stream()
                .filter(room -> authorizationService.hasPropertyAccess(
                        currentUser.getId(),
                        room.getFloor().getProperty().getId()))
                .toList();
        return ResponseEntity.ok(accessibleRooms);
    }

    @PostMapping("/rooms/bulk")
    public ResponseEntity<List<Room>> bulkCreateRooms(@Valid @RequestBody BulkRoomDto bulkRoomDto) {
        User currentUser = getCurrentUser();
        // Require write access via floor's property
        Long propertyId = authorizationService.getPropertyIdFromFloor(bulkRoomDto.getFloorId());
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);
        List<Room> rooms = propertyService.bulkCreateRooms(bulkRoomDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(rooms);
    }
}
