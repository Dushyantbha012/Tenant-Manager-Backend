package com.dushy.tenantmanage.service.impl;

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
        // Validate owner exists
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
    public Floor addFloor(FloorDto floorDto, Long propertyId) {
        // Validate property exists
        Properties property = propertiesRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property", propertyId));

        // Check for duplicate floor number
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
    public Room addRoom(RoomDto roomDto, Long floorId) {
        // Validate floor exists
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
}
