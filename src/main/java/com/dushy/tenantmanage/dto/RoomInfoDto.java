package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for room info with tenant and due details.
 * Used to provide enriched room data in a single API response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInfoDto {
    // Room fields
    private Long id;
    private String roomNumber;
    private RoomType roomType;
    private BigDecimal sizeSqft;
    private Boolean isOccupied;

    // Tenant fields (null if not occupied)
    private Long tenantId;
    private String tenantName;

    // Due amount for current month (0 if vacant or fully paid)
    private BigDecimal dueAmount;

    // Payment status: 'vacant', 'paid', 'due'
    private String paymentStatus;
}
