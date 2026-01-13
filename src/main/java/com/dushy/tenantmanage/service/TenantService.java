package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.RentAgreementDto;
import com.dushy.tenantmanage.dto.TenantDto;
import com.dushy.tenantmanage.entity.Tenant;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Tenant lifecycle management.
 * Handles tenant addition, move-out, and swapping.
 */
public interface TenantService {

    /**
     * Add a new tenant to a room.
     * Creates tenant record, marks room as occupied, and creates rent agreement.
     *
     * @param tenantDto    the tenant data
     * @param roomId       the ID of the room
     * @param agreementDto the rent agreement data
     * @param createdById  the ID of the user creating the record
     * @return the created tenant
     */
    Tenant addTenant(TenantDto tenantDto, Long roomId, RentAgreementDto agreementDto, Long createdById);

    /**
     * Move out a tenant.
     * Marks tenant as inactive, closes rent agreement, and frees up the room.
     *
     * @param tenantId the ID of the tenant
     * @return the updated tenant
     */
    Tenant moveOutTenant(Long tenantId);

    /**
     * Swap a tenant (atomic move-out and add-new operation).
     * Closes old tenant's agreement and creates new tenant in the same room.
     *
     * @param oldTenantId  the ID of the tenant moving out
     * @param newTenantDto the new tenant data
     * @param agreementDto the rent agreement for the new tenant
     * @param createdById  the ID of the user performing the swap
     * @return the new tenant
     */
    Tenant swapTenant(Long oldTenantId, TenantDto newTenantDto, RentAgreementDto agreementDto, Long createdById);

    /**
     * Get a tenant by ID.
     *
     * @param id the tenant ID
     * @return the tenant
     */
    Tenant getTenantById(Long id);

    /**
     * Get all active tenants.
     *
     * @return list of active tenants
     */
    List<Tenant> getActiveTenants();

    /**
     * Get the active tenant for a room.
     *
     * @param roomId the room ID
     * @return optional containing the tenant if found
     */
    Optional<Tenant> getActiveTenantByRoom(Long roomId);

    /**
     * Find a tenant by phone number.
     *
     * @param phone the phone number
     * @return optional containing the tenant if found
     */
    Optional<Tenant> getTenantByPhone(String phone);
}
