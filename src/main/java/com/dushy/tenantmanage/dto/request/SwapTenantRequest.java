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
 * Request wrapper for swapping tenants.
 * Contains new tenant data and rent agreement for atomic swap operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwapTenantRequest {

    @NotNull(message = "New tenant data is required")
    @Valid
    private TenantDto newTenant;

    @NotNull(message = "Rent agreement is required")
    @Valid
    private RentAgreementDto agreement;
}
