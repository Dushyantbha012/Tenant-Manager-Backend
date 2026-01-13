package com.dushy.tenantmanage.service;

import com.dushy.tenantmanage.dto.DueRentDto;
import com.dushy.tenantmanage.dto.RentAgreementDto;
import com.dushy.tenantmanage.dto.RentPaymentDto;
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
     *
     * @param agreementDto the agreement data
     * @param tenantId     the ID of the tenant
     * @param createdById  the ID of the user creating the agreement
     * @return the created rent agreement
     */
    RentAgreement createRentAgreement(RentAgreementDto agreementDto, Long tenantId, Long createdById);

    /**
     * Close an active rent agreement.
     * Sets end date to today and marks as inactive.
     *
     * @param agreementId the ID of the agreement
     * @return the closed agreement
     */
    RentAgreement closeRentAgreement(Long agreementId);

    /**
     * Record a rent payment.
     * Payments are immutable - never updated or deleted.
     *
     * @param paymentDto   the payment data
     * @param tenantId     the ID of the tenant
     * @param recordedById the ID of the user recording the payment
     * @return the recorded payment
     */
    RentPayment recordPayment(RentPaymentDto paymentDto, Long tenantId, Long recordedById);

    /**
     * Calculate due rent for a tenant for a specific month.
     * Formula: Expected Rent - Paid Amount
     *
     * @param tenantId the ID of the tenant
     * @param month    the month to calculate for (first day of month)
     * @return the due rent calculation
     */
    DueRentDto calculateDueRent(Long tenantId, LocalDate month);

    /**
     * Get due rent report for all active tenants for a month.
     *
     * @param month the month to generate report for
     * @return list of due rent calculations
     */
    List<DueRentDto> getDueRentReport(LocalDate month);

    /**
     * Get the active rent agreement for a tenant.
     *
     * @param tenantId the tenant ID
     * @return optional containing the active agreement if found
     */
    Optional<RentAgreement> getActiveAgreementByTenant(Long tenantId);

    /**
     * Get all payments for a tenant.
     *
     * @param tenantId the tenant ID
     * @return list of payments ordered by date descending
     */
    List<RentPayment> getPaymentsByTenant(Long tenantId);

    /**
     * Get all payments for a specific month.
     *
     * @param month the month (first day of month)
     * @return list of payments for the month
     */
    List<RentPayment> getPaymentsByMonth(LocalDate month);
}
