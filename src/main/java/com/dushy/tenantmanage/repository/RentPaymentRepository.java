package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.RentPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for RentPayment entity.
 * Manages immutable log of all financial transactions.
 */
@Repository
public interface RentPaymentRepository extends JpaRepository<RentPayment, Long> {

    /**
     * Find payment history for a specific tenant, ordered by payment date
     * descending.
     *
     * @param tenantId the ID of the tenant
     * @return list of payments ordered by most recent first
     */
    List<RentPayment> findByTenantIdOrderByPaymentDateDesc(Long tenantId);

    /**
     * Find all payments for a specific month.
     * Used for monthly collection reports.
     *
     * @param paymentMonth the month to search for (first day of the month)
     * @return list of payments for the specified month
     */
    List<RentPayment> findByPaymentForMonth(LocalDate paymentMonth);

    /**
     * Find all payments against a specific rent agreement.
     *
     * @param rentAgreementId the ID of the rent agreement
     * @return list of payments for the agreement
     */
    List<RentPayment> findByRentAgreementId(Long rentAgreementId);

    /**
     * Calculate total amount paid by a tenant for a specific month.
     * Custom query to sum payment amounts.
     *
     * @param tenantId the ID of the tenant
     * @param month    the month to calculate for
     * @return total amount paid, or null if no payments found
     */
    @Query("SELECT SUM(rp.amountPaid) FROM RentPayment rp WHERE rp.tenant.id = :tenantId AND rp.paymentForMonth = :month")
    BigDecimal sumAmountPaidByTenantIdAndPaymentForMonth(@Param("tenantId") Long tenantId,
            @Param("month") LocalDate month);
}
