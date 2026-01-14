package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.DashboardSummaryDto;
import com.dushy.tenantmanage.dto.TrendDataDto;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for dashboard and analytics.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CustomUserDetailsService userDetailsService;

    public DashboardController(DashboardService dashboardService,
            CustomUserDetailsService userDetailsService) {
        this.dashboardService = dashboardService;
        this.userDetailsService = userDetailsService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        User currentUser = getCurrentUser();
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(currentUser.getId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/summary/property/{propertyId}")
    public ResponseEntity<DashboardSummaryDto> getPropertySummary(@PathVariable Long propertyId) {
        DashboardSummaryDto summary = dashboardService.getPropertySummary(propertyId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/analytics/rent")
    public ResponseEntity<List<TrendDataDto>> getRentTrends(
            @RequestParam(defaultValue = "6") int months) {
        List<TrendDataDto> trends = dashboardService.getRentTrends(months);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/analytics/occupancy")
    public ResponseEntity<List<TrendDataDto>> getOccupancyTrends(
            @RequestParam(defaultValue = "6") int months) {
        List<TrendDataDto> trends = dashboardService.getOccupancyTrends(months);
        return ResponseEntity.ok(trends);
    }
}
