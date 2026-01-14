package com.dushy.tenantmanage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for due rent report.
 * Calculated dynamically as: Expected Rent - Paid Amount
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DueRentDto {
    private Long tenantId;
    private String tenantName;
    private String roomNumber;
    private Long propertyId;
    private String propertyName;
    private BigDecimal expectedAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private LocalDate month;
}
