package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDto {
    private String roomNumber;
    private RoomType roomType;
    private BigDecimal sizeSqft;
}
