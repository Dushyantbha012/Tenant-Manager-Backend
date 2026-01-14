package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.BulkPaymentDto;
import com.dushy.tenantmanage.dto.DueRentDto;
import com.dushy.tenantmanage.dto.RentAgreementDto;
import com.dushy.tenantmanage.dto.RentPaymentDto;
import com.dushy.tenantmanage.dto.RentPaymentResponseDto;
import com.dushy.tenantmanage.dto.RentSummaryDto;
import com.dushy.tenantmanage.entity.RentAgreement;
import com.dushy.tenantmanage.entity.RentPayment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Rent management.
 * Handles rent agreements, payments, and due calculations.
 */
public interface RentService {

    /**
     * Create a new rent agreement for a tenant.
     */
    RentAgreement createRentAgreement(RentAgreementDto agreementDto, Long tenantId, Long createdById);

    /**
     * Close an active rent agreement.
     */
    RentAgreement closeRentAgreement(Long agreementId);

    /**
     * Record a rent payment.
     */
    RentPayment recordPayment(RentPaymentDto paymentDto, Long tenantId, Long recordedById);

    /**
     * Calculate due rent for a tenant for a specific month.
     */
    DueRentDto calculateDueRent(Long tenantId, LocalDate month);

    /**
     * Get due rent report for all active tenants for a month.
     */
    List<DueRentDto> getDueRentReport(LocalDate month);

    /**
     * Get the active rent agreement for a tenant.
     */
    Optional<RentAgreement> getActiveAgreementByTenant(Long tenantId);

    /**
     * Get all payments for a tenant.
     */
    List<RentPayment> getPaymentsByTenant(Long tenantId);

    /**
     * Get all payments for a specific month.
     */
    List<RentPayment> getPaymentsByMonth(LocalDate month);

    /**
     * Get monthly rent collection summary for a property.
     *
     * @param propertyId the property ID
     * @return rent summary for current month
     */
    RentSummaryDto getRentSummaryByProperty(Long propertyId);

    /**
     * Search payments within a date range.
     *
     * @param startDate start date
     * @param endDate   end date
     * @return list of payments
     */
    List<RentPayment> searchPayments(LocalDate startDate, LocalDate endDate);

    /**
     * Search payments with optional property and room filters.
     * Returns flattened DTOs suitable for frontend display.
     *
     * @param startDate  start date
     * @param endDate    end date
     * @param propertyId optional property filter
     * @param roomId     optional room filter
     * @return list of payment response DTOs
     */
    List<RentPaymentResponseDto> searchPaymentsWithFilters(LocalDate startDate, LocalDate endDate, Long propertyId,
            Long roomId);

    /**
     * Record multiple payments in bulk.
     *
     * @param bulkPaymentDto the bulk payment data
     * @param recordedById   the user recording the payments
     * @return list of recorded payments
     */
    List<RentPayment> bulkRecordPayments(BulkPaymentDto bulkPaymentDto, Long recordedById);
}
