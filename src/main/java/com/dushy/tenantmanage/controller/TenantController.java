package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.request.CreateTenantRequest;
import com.dushy.tenantmanage.dto.request.SwapTenantRequest;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for tenant lifecycle management.
 * Handles tenant addition, move-out, and swapping.
 */
@RestController
@RequestMapping("/api")
public class TenantController {

    private final TenantService tenantService;
    private final CustomUserDetailsService userDetailsService;

    public TenantController(TenantService tenantService,
            CustomUserDetailsService userDetailsService) {
        this.tenantService = tenantService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get the currently authenticated user.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    /**
     * Add a new tenant to a room.
     * Creates tenant record, marks room as occupied, and creates rent agreement.
     *
     * @param request the tenant creation request
     * @return the created tenant
     */
    @PostMapping("/tenants")
    public ResponseEntity<Tenant> addTenant(@Valid @RequestBody CreateTenantRequest request) {
        User currentUser = getCurrentUser();
        Tenant tenant = tenantService.addTenant(
                request.getTenant(),
                request.getRoomId(),
                request.getAgreement(),
                currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
    }

    /**
     * Get all active tenants.
     *
     * @return list of active tenants
     */
    @GetMapping("/tenants")
    public ResponseEntity<List<Tenant>> getActiveTenants() {
        List<Tenant> tenants = tenantService.getActiveTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Get a tenant by ID.
     *
     * @param id the tenant ID
     * @return the tenant
     */
    @GetMapping("/tenants/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable Long id) {
        Tenant tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Move out a tenant (soft delete).
     * Marks tenant as inactive, closes rent agreement, and frees up the room.
     *
     * @param id the tenant ID
     * @return the updated tenant
     */
    @DeleteMapping("/tenants/{id}")
    public ResponseEntity<Tenant> moveOutTenant(@PathVariable Long id) {
        Tenant tenant = tenantService.moveOutTenant(id);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Swap a tenant (atomic move-out and add new).
     * Closes old tenant's agreement and creates new tenant in the same room.
     *
     * @param id      the ID of the tenant moving out
     * @param request the swap request containing new tenant data
     * @return the new tenant
     */
    @PostMapping("/tenants/{id}/swap")
    public ResponseEntity<Tenant> swapTenant(@PathVariable Long id,
            @Valid @RequestBody SwapTenantRequest request) {
        User currentUser = getCurrentUser();
        Tenant newTenant = tenantService.swapTenant(
                id,
                request.getNewTenant(),
                request.getAgreement(),
                currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(newTenant);
    }

    /**
     * Get the active tenant for a room.
     *
     * @param roomId the room ID
     * @return the tenant if found, or 204 No Content if room is vacant
     */
    @GetMapping("/rooms/{roomId}/tenant")
    public ResponseEntity<Tenant> getActiveTenantByRoom(@PathVariable Long roomId) {
        Optional<Tenant> tenant = tenantService.getActiveTenantByRoom(roomId);
        return tenant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
