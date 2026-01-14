package com.dushy.tenantmanage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for bulk floor creation.
 */
public class BulkFloorDto {

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Floors list is required")
    @Valid
    private List<FloorDto> floors;

    public BulkFloorDto() {
    }

    public BulkFloorDto(Long propertyId, List<FloorDto> floors) {
        this.propertyId = propertyId;
        this.floors = floors;
    }

    // Getters and Setters
    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public List<FloorDto> getFloors() {
        return floors;
    }

    public void setFloors(List<FloorDto> floors) {
        this.floors = floors;
    }
}
