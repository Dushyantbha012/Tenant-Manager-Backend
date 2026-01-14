package com.dushy.tenantmanage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for bulk room creation.
 */
public class BulkRoomDto {

    @NotNull(message = "Floor ID is required")
    private Long floorId;

    @NotNull(message = "Rooms list is required")
    @Valid
    private List<RoomDto> rooms;

    public BulkRoomDto() {
    }

    public BulkRoomDto(Long floorId, List<RoomDto> rooms) {
        this.floorId = floorId;
        this.rooms = rooms;
    }

    // Getters and Setters
    public Long getFloorId() {
        return floorId;
    }

    public void setFloorId(Long floorId) {
        this.floorId = floorId;
    }

    public List<RoomDto> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomDto> rooms) {
        this.rooms = rooms;
    }
}
