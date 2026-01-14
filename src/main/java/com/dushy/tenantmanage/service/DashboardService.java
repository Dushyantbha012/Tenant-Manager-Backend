package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.DashboardSummaryDto;
import com.dushy.tenantmanage.dto.TrendDataDto;

import java.util.List;

/**
 * Service interface for Dashboard and Analytics.
 */
public interface DashboardService {

    /**
     * Get overall dashboard summary for a user.
     *
     * @param userId the user ID
     * @return dashboard summary
     */
    DashboardSummaryDto getDashboardSummary(Long userId);

    /**
     * Get property-specific summary.
     *
     * @param propertyId the property ID
     * @return dashboard summary for the property
     */
    DashboardSummaryDto getPropertySummary(Long propertyId);

    /**
     * Get rent collection trends.
     *
     * @param months number of months to look back
     * @return list of trend data points
     */
    List<TrendDataDto> getRentTrends(int months);

    /**
     * Get occupancy trends.
     *
     * @param months number of months to look back
     * @return list of trend data points
     */
    List<TrendDataDto> getOccupancyTrends(int months);
}
