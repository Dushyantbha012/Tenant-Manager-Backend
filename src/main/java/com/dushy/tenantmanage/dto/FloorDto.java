package com.dushy.tenantmanage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FloorDto {
    private Integer floorNumber;
    private String floorName;
}
