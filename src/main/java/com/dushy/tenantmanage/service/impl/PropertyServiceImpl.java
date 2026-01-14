package com.dushy.tenantmanage.service.impl;

import com.dushy.tenantmanage.dto.BulkFloorDto;
import com.dushy.tenantmanage.dto.BulkRoomDto;
import com.dushy.tenantmanage.dto.FloorDto;
import com.dushy.tenantmanage.dto.PropertyDto;
import com.dushy.tenantmanage.dto.RoomDto;
import com.dushy.tenantmanage.entity.Floor;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.Room;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.exception.DuplicateResourceException;
import com.dushy.tenantmanage.exception.ResourceNotFoundException;
import com.dushy.tenantmanage.repository.FloorRepository;
import com.dushy.tenantmanage.repository.PropertiesRepository;
import com.dushy.tenantmanage.repository.RoomRepository;
import com.dushy.tenantmanage.repository.UserRepository;
import com.dushy.tenantmanage.service.PropertyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PropertyService.
 * Manages property hierarchy: Property -> Floor -> Room.
 */
@Service
@Transactional
public class PropertyServiceImpl implements PropertyService {

    private final PropertiesRepository propertiesRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public PropertyServiceImpl(PropertiesRepository propertiesRepository,
            FloorRepository floorRepository,
            RoomRepository roomRepository,
            UserRepository userRepository) {
        this.propertiesRepository = propertiesRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Properties createProperty(PropertyDto propertyDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", ownerId));

        Properties property = Properties.builder()
                .owner(owner)
                .name(propertyDto.getName())
                .address(propertyDto.getAddress())
                .city(propertyDto.getCity())
                .state(propertyDto.getState())
                .postalCode(propertyDto.getPostalCode())
                .country(propertyDto.getCountry())
                .totalFloors(propertyDto.getTotalFloors())
                .isActive(true)
                .build();

        return propertiesRepository.save(property);
    }

    @Override
    public Properties updateProperty(Long id, PropertyDto propertyDto) {
        Properties property = getPropertyById(id);
        property.setName(propertyDto.getName());
        property.setAddress(propertyDto.getAddress());
        property.setCity(propertyDto.getCity());
        property.setState(propertyDto.getState());
        property.setPostalCode(propertyDto.getPostalCode());
        property.setCountry(propertyDto.getCountry());
        property.setTotalFloors(propertyDto.getTotalFloors());
        return propertiesRepository.save(property);
    }

    @Override
    public void deleteProperty(Long id) {
        Properties property = getPropertyById(id);
        property.setIsActive(false);
        propertiesRepository.save(property);
    }

    @Override
    public Floor addFloor(FloorDto floorDto, Long propertyId) {
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        if (floorRepository.findByPropertyIdAndFloorNumber(propertyId, floorDto.getFloorNumber()).isPresent()) {
            throw new DuplicateResourceException("Floor", "floor number", floorDto.getFloorNumber().toString());
        }

        Floor floor = Floor.builder()
                .property(property)
                .floorNumber(floorDto.getFloorNumber())
                .floorName(floorDto.getFloorName())
                .isActive(true)
                .build();

        return floorRepository.save(floor);
    }

    @Override
    @Transactional(readOnly = true)
    public Floor getFloorById(Long id) {
        return floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", id));
    }

    @Override
    public Floor updateFloor(Long id, FloorDto floorDto) {
        Floor floor = getFloorById(id);
        floor.setFloorNumber(floorDto.getFloorNumber());
        floor.setFloorName(floorDto.getFloorName());
        return floorRepository.save(floor);
    }

    @Override
    public void deleteFloor(Long id) {
        Floor floor = getFloorById(id);
        floor.setIsActive(false);
        floorRepository.save(floor);
    }

    @Override
    public Room addRoom(RoomDto roomDto, Long floorId) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new ResourceNotFoundException("Floor", floorId));

        Room room = Room.builder()
                .floor(floor)
                .roomNumber(roomDto.getRoomNumber())
                .roomType(roomDto.getRoomType())
                .sizeSqft(roomDto.getSizeSqft())
                .isOccupied(false)
                .isActive(true)
                .build();

        return roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    @Override
    public Room updateRoom(Long id, RoomDto roomDto) {
        Room room = getRoomById(id);
        room.setRoomNumber(roomDto.getRoomNumber());
        room.setRoomType(roomDto.getRoomType());
        room.setSizeSqft(roomDto.getSizeSqft());
        return roomRepository.save(room);
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        room.setIsActive(false);
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Properties> getPropertiesByOwner(Long ownerId) {
        return propertiesRepository.findByOwnerId(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Properties getPropertyById(Long id) {
        return propertiesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Floor> getFloorsByProperty(Long propertyId) {
        return floorRepository.findByPropertyIdOrderByFloorNumberAsc(propertyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getRoomsByFloor(Long floorId) {
        return roomRepository.findByFloorId(floorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getAvailableRoomsByFloor(Long floorId) {
        return roomRepository.findByFloorIdAndIsOccupiedFalse(floorId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getRoomsByProperty(Long propertyId) {
        return roomRepository.findByFloorPropertyId(propertyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> getVacantRooms() {
        return roomRepository.findByIsOccupiedFalseAndIsActiveTrue();
    }

    @Override
    public List<Floor> bulkCreateFloors(BulkFloorDto bulkFloorDto) {
        Properties property = propertiesRepository.findById(bulkFloorDto.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property", bulkFloorDto.getPropertyId()));

        List<Floor> createdFloors = new ArrayList<>();
        for (FloorDto floorDto : bulkFloorDto.getFloors()) {
            if (floorRepository.findByPropertyIdAndFloorNumber(bulkFloorDto.getPropertyId(),
                    floorDto.getFloorNumber()).isEmpty()) {
                Floor floor = Floor.builder()
                        .property(property)
                        .floorNumber(floorDto.getFloorNumber())
                        .floorName(floorDto.getFloorName())
                        .isActive(true)
                        .build();
                createdFloors.add(floorRepository.save(floor));
            }
        }
        return createdFloors;
    }

    @Override
    public List<Room> bulkCreateRooms(BulkRoomDto bulkRoomDto) {
        Floor floor = floorRepository.findById(bulkRoomDto.getFloorId())
                .orElseThrow(() -> new ResourceNotFoundException("Floor", bulkRoomDto.getFloorId()));

        List<Room> createdRooms = new ArrayList<>();
        for (RoomDto roomDto : bulkRoomDto.getRooms()) {
            Room room = Room.builder()
                    .floor(floor)
                    .roomNumber(roomDto.getRoomNumber())
                    .roomType(roomDto.getRoomType())
                    .sizeSqft(roomDto.getSizeSqft())
                    .isOccupied(false)
                    .isActive(true)
                    .build();
            createdRooms.add(roomRepository.save(room));
        }
        return createdRooms;
    }
}
