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

        /**
         * Calculate total amount paid by a tenant between two months (inclusive).
         * Used for cumulative due calculation.
         *
         * @param tenantId   the ID of the tenant
         * @param startMonth first month of the range
         * @param endMonth   last month of the range
         * @return total amount paid, or null if no payments found
         */
        @Query("SELECT SUM(rp.amountPaid) FROM RentPayment rp WHERE rp.tenant.id = :tenantId AND rp.paymentForMonth >= :startMonth AND rp.paymentForMonth <= :endMonth")
        BigDecimal sumAmountPaidByTenantIdBetweenMonths(@Param("tenantId") Long tenantId,
                        @Param("startMonth") LocalDate startMonth,
                        @Param("endMonth") LocalDate endMonth);

        /**
         * Find payments within a date range.
         *
         * @param startDate start of date range
         * @param endDate   end of date range
         * @return list of payments within the range
         */
        List<RentPayment> findByPaymentDateBetween(LocalDate startDate, LocalDate endDate);

        /**
         * Sum all payments for a specific month.
         *
         * @param month the month to sum for
         * @return total amount collected
         */
        @Query("SELECT COALESCE(SUM(rp.amountPaid), 0) FROM RentPayment rp WHERE rp.paymentForMonth = :month")
        BigDecimal sumAmountPaidByPaymentForMonth(@Param("month") LocalDate month);

        /**
         * Find payments by property for a specific month.
         *
         * @param propertyId the property ID
         * @param month      the month to filter by
         * @return list of payments
         */
        @Query("SELECT rp FROM RentPayment rp WHERE rp.tenant.room.floor.property.id = :propertyId AND rp.paymentForMonth = :month")
        List<RentPayment> findByPropertyIdAndPaymentForMonth(@Param("propertyId") Long propertyId,
                        @Param("month") LocalDate month);

        /**
         * Sum payments for a property in a specific month.
         *
         * @param propertyId the property ID
         * @param month      the month
         * @return total collected
         */
        @Query("SELECT COALESCE(SUM(rp.amountPaid), 0) FROM RentPayment rp WHERE rp.tenant.room.floor.property.id = :propertyId AND rp.paymentForMonth = :month")
        BigDecimal sumAmountPaidByPropertyIdAndPaymentForMonth(@Param("propertyId") Long propertyId,
                        @Param("month") LocalDate month);

        /**
         * Find payments within a date range filtered by property.
         *
         * @param startDate  start of date range
         * @param endDate    end of date range
         * @param propertyId the property ID to filter by
         * @return list of payments within the range for the property
         */
        @Query("SELECT rp FROM RentPayment rp WHERE rp.paymentDate BETWEEN :startDate AND :endDate AND rp.tenant.room.floor.property.id = :propertyId ORDER BY rp.paymentDate DESC")
        List<RentPayment> findByPaymentDateBetweenAndPropertyId(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("propertyId") Long propertyId);

        /**
         * Find payments within a date range filtered by room.
         *
         * @param startDate start of date range
         * @param endDate   end of date range
         * @param roomId    the room ID to filter by
         * @return list of payments within the range for the room
         */
        @Query("SELECT rp FROM RentPayment rp WHERE rp.paymentDate BETWEEN :startDate AND :endDate AND rp.tenant.room.id = :roomId ORDER BY rp.paymentDate DESC")
        List<RentPayment> findByPaymentDateBetweenAndRoomId(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("roomId") Long roomId);

        /**
         * Find payments within a date range filtered by property and room.
         *
         * @param startDate  start of date range
         * @param endDate    end of date range
         * @param propertyId the property ID to filter by
         * @param roomId     the room ID to filter by
         * @return list of payments within the range
         */
        @Query("SELECT rp FROM RentPayment rp WHERE rp.paymentDate BETWEEN :startDate AND :endDate AND rp.tenant.room.floor.property.id = :propertyId AND rp.tenant.room.id = :roomId ORDER BY rp.paymentDate DESC")
        List<RentPayment> findByPaymentDateBetweenAndPropertyIdAndRoomId(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("propertyId") Long propertyId,
                        @Param("roomId") Long roomId);
}
