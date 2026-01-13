package com.dushy.tenantmanage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDto {
    private String name;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private Integer totalFloors;
}
