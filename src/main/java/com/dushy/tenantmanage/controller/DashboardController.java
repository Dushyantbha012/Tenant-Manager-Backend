package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.DashboardSummaryDto;
import com.dushy.tenantmanage.dto.TrendDataDto;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.security.PropertyAuthorizationService;
import com.dushy.tenantmanage.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for dashboard and analytics.
 * All endpoints are secured with property-level authorization.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CustomUserDetailsService userDetailsService;
    private final PropertyAuthorizationService authorizationService;

    public DashboardController(DashboardService dashboardService,
            CustomUserDetailsService userDetailsService,
            PropertyAuthorizationService authorizationService) {
        this.dashboardService = dashboardService;
        this.userDetailsService = userDetailsService;
        this.authorizationService = authorizationService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // The JwtAuthenticationFilter now stores the User entity as the principal
        return (User) auth.getPrincipal();
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        User currentUser = getCurrentUser();
        // Already scoped by userId - only shows user's accessible properties
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(currentUser.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/property/{propertyId}")
    public ResponseEntity<DashboardSummaryDto> getPropertySummary(@PathVariable Long propertyId) {
        User currentUser = getCurrentUser();
        // Check read access to property
        authorizationService.checkPropertyAccess(currentUser.getId(), propertyId);

        DashboardSummaryDto summary = dashboardService.getPropertySummary(propertyId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/analytics/rent")
    public ResponseEntity<List<TrendDataDto>> getRentTrends(
            @RequestParam(defaultValue = "6") int months) {
        User currentUser = getCurrentUser();
        // Get rent trends for user's accessible properties only
        List<TrendDataDto> trends = dashboardService.getRentTrendsForUser(currentUser.getId(), months);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/analytics/occupancy")
    public ResponseEntity<List<TrendDataDto>> getOccupancyTrends(
            @RequestParam(defaultValue = "6") int months) {
        User currentUser = getCurrentUser();
        // Get occupancy trends for user's accessible properties only
        List<TrendDataDto> trends = dashboardService.getOccupancyTrendsForUser(currentUser.getId(), months);
        return ResponseEntity.ok(trends);
    }
}
