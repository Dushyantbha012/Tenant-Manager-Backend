package com.dushy.tenantmanage.service.impl;

import com.dushy.tenantmanage.dto.DashboardSummaryDto;
import com.dushy.tenantmanage.dto.TrendDataDto;
import com.dushy.tenantmanage.entity.Properties;
import com.dushy.tenantmanage.entity.RentAgreement;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.repository.*;
import com.dushy.tenantmanage.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DashboardService.
 */
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PropertiesRepository propertiesRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;
    private final RentAgreementRepository rentAgreementRepository;
    private final RentPaymentRepository rentPaymentRepository;

    public DashboardServiceImpl(PropertiesRepository propertiesRepository,
            RoomRepository roomRepository,
            TenantRepository tenantRepository,
            RentAgreementRepository rentAgreementRepository,
            RentPaymentRepository rentPaymentRepository) {
        this.propertiesRepository = propertiesRepository;
        this.roomRepository = roomRepository;
        this.tenantRepository = tenantRepository;
        this.rentAgreementRepository = rentAgreementRepository;
        this.rentPaymentRepository = rentPaymentRepository;
    }

    @Override
    public DashboardSummaryDto getDashboardSummary(Long userId) {
        List<Properties> properties = propertiesRepository.findByOwnerId(userId);
        int totalProperties = properties.size();

        long totalRooms = roomRepository.countByIsActiveTrue();
        long occupiedRooms = roomRepository.countByIsOccupiedTrueAndIsActiveTrue();
        long vacantRooms = totalRooms - occupiedRooms;

        long totalTenants = tenantRepository.countByIsActiveTrue();

        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        BigDecimal totalRentExpected = calculateTotalExpectedRent();
        BigDecimal totalRentCollected = rentPaymentRepository.sumAmountPaidByPaymentForMonth(currentMonth);
        if (totalRentCollected == null) {
            totalRentCollected = BigDecimal.ZERO;
        }

        double occupancyRate = totalRooms > 0 ? ((double) occupiedRooms / totalRooms) * 100 : 0;

        return new DashboardSummaryDto(
                totalProperties,
                (int) totalRooms,
                (int) occupiedRooms,
                (int) vacantRooms,
                (int) totalTenants,
                totalRentExpected,
                totalRentCollected,
                occupancyRate);
    }

    @Override
    public DashboardSummaryDto getPropertySummary(Long propertyId) {
        long totalRooms = roomRepository.countByFloorPropertyId(propertyId);
        long occupiedRooms = roomRepository.countByFloorPropertyIdAndIsOccupiedTrue(propertyId);
        long vacantRooms = totalRooms - occupiedRooms;

        List<Tenant> tenants = tenantRepository.findByIsActiveTrueAndRoomFloorPropertyId(propertyId);
        int totalTenants = tenants.size();

        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        BigDecimal expected = BigDecimal.ZERO;
        for (Tenant tenant : tenants) {
            Optional<RentAgreement> agreement = rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenant.getId());
            if (agreement.isPresent()) {
                expected = expected.add(agreement.get().getMonthlyRentAmount());
            }
        }

        BigDecimal collected = rentPaymentRepository.sumAmountPaidByPropertyIdAndPaymentForMonth(propertyId,
                currentMonth);
        if (collected == null) {
            collected = BigDecimal.ZERO;
        }

        double occupancyRate = totalRooms > 0 ? ((double) occupiedRooms / totalRooms) * 100 : 0;

        return new DashboardSummaryDto(
                1,
                (int) totalRooms,
                (int) occupiedRooms,
                (int) vacantRooms,
                totalTenants,
                expected,
                collected,
                occupancyRate);
    }

    @Override
    public List<TrendDataDto> getRentTrends(int months) {
        List<TrendDataDto> trends = new ArrayList<>();
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);

        for (int i = 0; i < months; i++) {
            LocalDate month = startMonth.plusMonths(i);
            BigDecimal collected = rentPaymentRepository.sumAmountPaidByPaymentForMonth(month);
            if (collected == null) {
                collected = BigDecimal.ZERO;
            }
            trends.add(new TrendDataDto(month, collected));
        }

        return trends;
    }

    @Override
    public List<TrendDataDto> getOccupancyTrends(int months) {
        // Simplified: returns current occupancy for all months (would need historical
        // data for accurate trends)
        List<TrendDataDto> trends = new ArrayList<>();
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);

        long totalRooms = roomRepository.countByIsActiveTrue();
        long occupiedRooms = roomRepository.countByIsOccupiedTrueAndIsActiveTrue();
        BigDecimal occupancyRate = totalRooms > 0
                ? BigDecimal.valueOf((double) occupiedRooms / totalRooms * 100)
                : BigDecimal.ZERO;

        for (int i = 0; i < months; i++) {
            LocalDate month = startMonth.plusMonths(i);
            trends.add(new TrendDataDto(month, occupancyRate));
        }

        return trends;
    }

    @Override
    public List<TrendDataDto> getRentTrendsForUser(Long userId, int months) {
        List<TrendDataDto> trends = new ArrayList<>();
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);

        // Get user's accessible properties
        List<Properties> userProperties = propertiesRepository.findByOwnerId(userId);

        for (int i = 0; i < months; i++) {
            LocalDate month = startMonth.plusMonths(i);
            BigDecimal collected = BigDecimal.ZERO;

            // Sum payments across user's properties
            for (Properties property : userProperties) {
                BigDecimal propertyCollected = rentPaymentRepository
                        .sumAmountPaidByPropertyIdAndPaymentForMonth(property.getId(), month);
                if (propertyCollected != null) {
                    collected = collected.add(propertyCollected);
                }
            }

            trends.add(new TrendDataDto(month, collected));
        }

        return trends;
    }

    @Override
    public List<TrendDataDto> getOccupancyTrendsForUser(Long userId, int months) {
        List<TrendDataDto> trends = new ArrayList<>();
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1);

        // Get user's accessible properties
        List<Properties> userProperties = propertiesRepository.findByOwnerId(userId);

        long totalRooms = 0;
        long occupiedRooms = 0;

        for (Properties property : userProperties) {
            totalRooms += roomRepository.countByFloorPropertyId(property.getId());
            occupiedRooms += roomRepository.countByFloorPropertyIdAndIsOccupiedTrue(property.getId());
        }

        BigDecimal occupancyRate = totalRooms > 0
                ? BigDecimal.valueOf((double) occupiedRooms / totalRooms * 100)
                : BigDecimal.ZERO;

        for (int i = 0; i < months; i++) {
            LocalDate month = startMonth.plusMonths(i);
            trends.add(new TrendDataDto(month, occupancyRate));
        }

        return trends;
    }

    private BigDecimal calculateTotalExpectedRent() {
        List<Tenant> allTenants = tenantRepository.findByIsActiveTrue();
        BigDecimal total = BigDecimal.ZERO;

        for (Tenant tenant : allTenants) {
            Optional<RentAgreement> agreement = rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenant.getId());
            if (agreement.isPresent()) {
                total = total.add(agreement.get().getMonthlyRentAmount());
            }
        }

        return total;
    }
}
