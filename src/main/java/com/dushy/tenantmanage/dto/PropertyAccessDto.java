package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyAccessDto {
    private Long propertyId;
    private Long userId;
    private AccessLevel accessLevel;
}
