package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.RentAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RentAgreement entity.
 * Manages legal and financial agreements.
 */
@Repository
public interface RentAgreementRepository extends JpaRepository<RentAgreement, Long> {

    /**
     * Find the currently active agreement for a tenant.
     *
     * @param tenantId the ID of the tenant
     * @return Optional containing the active rent agreement if found
     */
    Optional<RentAgreement> findByTenantIdAndIsActiveTrue(Long tenantId);

    /**
     * Find all active rent agreements.
     * Used for billing cycles and active agreement management.
     *
     * @return list of all active rent agreements
     */
    List<RentAgreement> findAllByIsActiveTrue();

    /**
     * Find agreements nearing expiration within a date range.
     * Useful for renewal notifications.
     *
     * @param start the start date of the range
     * @param end   the end date of the range
     * @return list of agreements ending within the date range
     */
    List<RentAgreement> findByEndDateBetween(LocalDate start, LocalDate end);
}
