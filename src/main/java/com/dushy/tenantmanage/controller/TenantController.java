package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.RentAgreementDto;
import com.dushy.tenantmanage.dto.TenantDto;
import com.dushy.tenantmanage.dto.TenantResponseDto;
import com.dushy.tenantmanage.dto.request.CreateTenantRequest;
import com.dushy.tenantmanage.dto.request.SwapTenantRequest;
import com.dushy.tenantmanage.entity.RentAgreement;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.security.PropertyAuthorizationService;
import com.dushy.tenantmanage.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * REST Controller for tenant lifecycle management.
 * All endpoints are secured with property-level authorization.
 */
@RestController
@RequestMapping("/api")
public class TenantController {

    private final TenantService tenantService;
    private final CustomUserDetailsService userDetailsService;
    private final PropertyAuthorizationService authorizationService;

    public TenantController(TenantService tenantService,
            CustomUserDetailsService userDetailsService,
            PropertyAuthorizationService authorizationService) {
        this.tenantService = tenantService;
        this.userDetailsService = userDetailsService;
        this.authorizationService = authorizationService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // The JwtAuthenticationFilter now stores the User entity as the principal
        return (User) auth.getPrincipal();
    }

    @PostMapping("/tenants")
    public ResponseEntity<TenantResponseDto> addTenant(@Valid @RequestBody CreateTenantRequest request) {
        User currentUser = getCurrentUser();
        // Require write access to the room's property
        Long propertyId = authorizationService.getPropertyIdFromRoom(request.getRoomId());
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);

        Tenant tenant = tenantService.addTenant(
                request.getTenant(),
                request.getRoomId(),
                request.getAgreement(),
                currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.toResponseDto(tenant));
    }

    @GetMapping("/tenants")
    public ResponseEntity<List<TenantResponseDto>> getActiveTenants() {
        User currentUser = getCurrentUser();
        // Get all active tenants, then filter by accessible properties
        List<Tenant> allTenants = tenantService.getActiveTenants();
        Set<Long> accessiblePropertyIds = authorizationService.getAccessiblePropertyIds(currentUser.getId());

        List<Tenant> accessibleTenants = allTenants.stream()
                .filter(tenant -> accessiblePropertyIds.contains(
                        tenant.getRoom().getFloor().getProperty().getId()))
                .toList();
        return ResponseEntity.ok(tenantService.toResponseDtos(accessibleTenants));
    }

    @GetMapping("/tenants/{id}")
    public ResponseEntity<TenantResponseDto> getTenantById(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Check access via tenant's property
        Long propertyId = authorizationService.getPropertyIdFromTenant(id);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);

        Tenant tenant = tenantService.getTenantById(id);
        return ResponseEntity.ok(tenantService.toResponseDto(tenant));
    }

    @PutMapping("/tenants/{id}")
    public ResponseEntity<TenantResponseDto> updateTenant(@PathVariable Long id,
            @Valid @RequestBody TenantDto tenantDto) {
        User currentUser = getCurrentUser();
        // Require write access to tenant's property
        Long propertyId = authorizationService.getPropertyIdFromTenant(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);

        Tenant tenant = tenantService.updateTenant(id, tenantDto);
        return ResponseEntity.ok(tenantService.toResponseDto(tenant));
    }

    @DeleteMapping("/tenants/{id}")
    public ResponseEntity<TenantResponseDto> moveOutTenant(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        // Require write access to tenant's property
        Long propertyId = authorizationService.getPropertyIdFromTenant(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);

        Tenant tenant = tenantService.moveOutTenant(id);
        return ResponseEntity.ok(tenantService.toResponseDto(tenant));
    }

    @PostMapping("/tenants/{id}/swap")
    public ResponseEntity<TenantResponseDto> swapTenant(@PathVariable Long id,
            @Valid @RequestBody SwapTenantRequest request) {
        User currentUser = getCurrentUser();
        // Require write access to tenant's property
        Long propertyId = authorizationService.getPropertyIdFromTenant(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);

        Tenant newTenant = tenantService.swapTenant(
                id,
                request.getNewTenant(),
                request.getAgreement(),
                currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.toResponseDto(newTenant));
    }

    @PutMapping("/tenants/{id}/agreement")
    public ResponseEntity<RentAgreement> updateAgreement(@PathVariable Long id,
            @Valid @RequestBody RentAgreementDto agreementDto) {
        User currentUser = getCurrentUser();
        // Require write access to tenant's property
        Long propertyId = authorizationService.getPropertyIdFromTenant(id);
        authorizationService.checkPropertyWriteAccess(currentUser.getId(), propertyId);

        RentAgreement agreement = tenantService.updateAgreement(id, agreementDto);
        return ResponseEntity.ok(agreement);
    }

    @GetMapping("/tenants/search")
    public ResponseEntity<List<TenantResponseDto>> searchTenants(
            @RequestParam String query,
            @RequestParam(required = false) Long propertyId) {
        User currentUser = getCurrentUser();

        if (propertyId != null) {
            // If searching in a specific property, check access
            authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);
            List<Tenant> tenants = tenantService.searchTenants(query, propertyId);
            return ResponseEntity.ok(tenantService.toResponseDtos(tenants));
        }

        // Search across all tenants, then filter by accessible properties
        List<Tenant> allTenants = tenantService.searchTenants(query, null);
        Set<Long> accessiblePropertyIds = authorizationService.getAccessiblePropertyIds(currentUser.getId());

        List<Tenant> accessibleTenants = allTenants.stream()
                .filter(tenant -> accessiblePropertyIds.contains(
                        tenant.getRoom().getFloor().getProperty().getId()))
                .toList();
        return ResponseEntity.ok(tenantService.toResponseDtos(accessibleTenants));
    }

    @GetMapping("/properties/{propertyId}/tenants")
    public ResponseEntity<List<TenantResponseDto>> getTenantsByProperty(@PathVariable Long propertyId) {
        User currentUser = getCurrentUser();
        // Check access to the property
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);

        List<Tenant> tenants = tenantService.getTenantsByProperty(propertyId);
        return ResponseEntity.ok(tenantService.toResponseDtos(tenants));
    }

    @GetMapping("/rooms/{roomId}/tenant")
    public ResponseEntity<TenantResponseDto> getActiveTenantByRoom(@PathVariable Long roomId) {
        User currentUser = getCurrentUser();
        // Check access via room's property
        Long propertyId = authorizationService.getPropertyIdFromRoom(roomId);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);

        Optional<Tenant> tenant = tenantService.getActiveTenantByRoom(roomId);
        return tenant.map(t -> ResponseEntity.ok(tenantService.toResponseDto(t)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/rooms/{roomId}/tenant-history")
    public ResponseEntity<List<TenantResponseDto>> getTenantHistoryByRoom(@PathVariable Long roomId) {
        User currentUser = getCurrentUser();
        // Check access via room's property
        Long propertyId = authorizationService.getPropertyIdFromRoom(roomId);
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);

        List<TenantResponseDto> history = tenantService.getTenantHistoryByRoom(roomId);
        return ResponseEntity.ok(history);
    }
}
