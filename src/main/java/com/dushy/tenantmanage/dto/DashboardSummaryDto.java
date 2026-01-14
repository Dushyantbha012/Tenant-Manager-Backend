package com.dushy.tenantmanage.dto;

import java.math.BigDecimal;

/**
 * DTO for dashboard summary statistics.
 */
public class DashboardSummaryDto {
    private int totalProperties;
    private int totalRooms;
    private int occupiedRooms;
    private int vacantRooms;
    private int totalTenants;
    private BigDecimal totalRentExpected;
    private BigDecimal totalRentCollected;
    private double occupancyRate;

    public DashboardSummaryDto() {
    }

    public DashboardSummaryDto(int totalProperties, int totalRooms, int occupiedRooms,
            int vacantRooms, int totalTenants, BigDecimal totalRentExpected,
            BigDecimal totalRentCollected, double occupancyRate) {
        this.totalProperties = totalProperties;
        this.totalRooms = totalRooms;
        this.occupiedRooms = occupiedRooms;
        this.vacantRooms = vacantRooms;
        this.totalTenants = totalTenants;
        this.totalRentExpected = totalRentExpected;
        this.totalRentCollected = totalRentCollected;
        this.occupancyRate = occupancyRate;
    }

    // Getters and Setters
    public int getTotalProperties() {
        return totalProperties;
    }

    public void setTotalProperties(int totalProperties) {
        this.totalProperties = totalProperties;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getOccupiedRooms() {
        return occupiedRooms;
    }

    public void setOccupiedRooms(int occupiedRooms) {
        this.occupiedRooms = occupiedRooms;
    }

    public int getVacantRooms() {
        return vacantRooms;
    }

    public void setVacantRooms(int vacantRooms) {
        this.vacantRooms = vacantRooms;
    }

    public int getTotalTenants() {
        return totalTenants;
    }

    public void setTotalTenants(int totalTenants) {
        this.totalTenants = totalTenants;
    }

    public BigDecimal getTotalRentExpected() {
        return totalRentExpected;
    }

    public void setTotalRentExpected(BigDecimal totalRentExpected) {
        this.totalRentExpected = totalRentExpected;
    }

    public BigDecimal getTotalRentCollected() {
        return totalRentCollected;
    }

    public void setTotalRentCollected(BigDecimal totalRentCollected) {
        this.totalRentCollected = totalRentCollected;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }
}
