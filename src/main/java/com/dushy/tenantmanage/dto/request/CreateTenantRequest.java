package com.dushy.tenantmanage.dto.request;

import com.dushy.tenantmanage.dto.RentAgreementDto;
import com.dushy.tenantmanage.dto.TenantDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request wrapper for creating a new tenant.
 * Combines tenant data, room assignment, and rent agreement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTenantRequest {

    @NotNull(message = "Tenant data is required")
    @Valid
    private TenantDto tenant;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Rent agreement is required")
    @Valid
    private RentAgreementDto agreement;
}
