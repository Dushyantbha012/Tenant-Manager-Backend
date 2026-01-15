package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.IdProofType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for tenants with flattened property, room, and agreement fields.
 * Used for tenant list displays where nested entity data is needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantResponseDto {
    // Tenant basic info
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private IdProofType idProofType;
    private String idProofNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDate moveInDate;
    private LocalDate moveOutDate;
    private Boolean isActive;
    private String status; // "ACTIVE" or "MOVED_OUT"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Room info (flattened)
    private Long roomId;
    private String roomNumber;

    // Property info (flattened)
    private Long propertyId;
    private String propertyName;

    // Rent Agreement info (flattened from active agreement)
    private BigDecimal rentAmount;
    private BigDecimal securityDeposit;
    private Integer paymentDueDay;
}
