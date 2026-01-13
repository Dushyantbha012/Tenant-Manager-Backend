package com.dushy.tenantmanage.service.impl;

import com.dushy.tenantmanage.dto.DueRentDto;
import com.dushy.tenantmanage.dto.RentAgreementDto;
import com.dushy.tenantmanage.dto.RentPaymentDto;
import com.dushy.tenantmanage.entity.RentAgreement;
import com.dushy.tenantmanage.entity.RentPayment;
import com.dushy.tenantmanage.entity.Tenant;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.exception.InvalidOperationException;
import com.dushy.tenantmanage.exception.ResourceNotFoundException;
import com.dushy.tenantmanage.repository.RentAgreementRepository;
import com.dushy.tenantmanage.repository.RentPaymentRepository;
import com.dushy.tenantmanage.repository.TenantRepository;
import com.dushy.tenantmanage.repository.UserRepository;
import com.dushy.tenantmanage.service.RentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of RentService.
 * Handles rent agreements, payments, and due calculations.
 */
@Service
@Transactional
public class RentServiceImpl implements RentService {

    private final RentAgreementRepository rentAgreementRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public RentServiceImpl(RentAgreementRepository rentAgreementRepository,
            RentPaymentRepository rentPaymentRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {
        this.rentAgreementRepository = rentAgreementRepository;
        this.rentPaymentRepository = rentPaymentRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RentAgreement createRentAgreement(RentAgreementDto agreementDto, Long tenantId, Long createdById) {
        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        // Check if tenant already has an active agreement
        if (rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenantId).isPresent()) {
            throw new InvalidOperationException("Tenant already has an active rent agreement");
        }

        // Validate creator exists
        User createdBy = userRepository.findById(createdById)
                .orElseThrow(() -> new ResourceNotFoundException("User", createdById));

        RentAgreement agreement = RentAgreement.builder()
                .tenant(tenant)
                .monthlyRentAmount(agreementDto.getMonthlyRentAmount())
                .securityDeposit(agreementDto.getSecurityDeposit())
                .startDate(agreementDto.getStartDate() != null ? agreementDto.getStartDate() : LocalDate.now())
                .paymentDueDay(agreementDto.getPaymentDueDay() != null ? agreementDto.getPaymentDueDay() : 1)
                .isActive(true)
                .createdBy(createdBy)
                .build();

        return rentAgreementRepository.save(agreement);
    }

    @Override
    public RentAgreement closeRentAgreement(Long agreementId) {
        RentAgreement agreement = rentAgreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("RentAgreement", agreementId));

        if (Boolean.FALSE.equals(agreement.getIsActive())) {
            throw new InvalidOperationException("Rent agreement is already closed");
        }

        agreement.setIsActive(false);
        agreement.setEndDate(LocalDate.now());

        return rentAgreementRepository.save(agreement);
    }

    @Override
    public RentPayment recordPayment(RentPaymentDto paymentDto, Long tenantId, Long recordedById) {
        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        // Get active rent agreement
        RentAgreement agreement = rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new InvalidOperationException("No active rent agreement found for tenant"));

        // Validate recorder exists
        User recordedBy = userRepository.findById(recordedById)
                .orElseThrow(() -> new ResourceNotFoundException("User", recordedById));

        RentPayment payment = RentPayment.builder()
                .rentAgreement(agreement)
                .tenant(tenant)
                .amountPaid(paymentDto.getAmountPaid())
                .paymentDate(paymentDto.getPaymentDate() != null ? paymentDto.getPaymentDate() : LocalDate.now())
                .paymentForMonth(paymentDto.getPaymentForMonth())
                .paymentMode(paymentDto.getPaymentMode())
                .transactionReference(paymentDto.getTransactionReference())
                .notes(paymentDto.getNotes())
                .recordedBy(recordedBy)
                .build();

        return rentPaymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public DueRentDto calculateDueRent(Long tenantId, LocalDate month) {
        // Validate tenant exists
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", tenantId));

        // Get active rent agreement
        RentAgreement agreement = rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenantId)
                .orElseThrow(() -> new InvalidOperationException("No active rent agreement found for tenant"));

        BigDecimal expectedAmount = agreement.getMonthlyRentAmount();

        // Get total paid for the month
        BigDecimal paidAmount = rentPaymentRepository.sumAmountPaidByTenantIdAndPaymentForMonth(tenantId, month);
        if (paidAmount == null) {
            paidAmount = BigDecimal.ZERO;
        }

        // Calculate due: Expected - Paid
        BigDecimal dueAmount = expectedAmount.subtract(paidAmount);

        return DueRentDto.builder()
                .tenantId(tenantId)
                .tenantName(tenant.getFullName())
                .roomNumber(tenant.getRoom().getRoomNumber())
                .expectedAmount(expectedAmount)
                .paidAmount(paidAmount)
                .dueAmount(dueAmount)
                .month(month)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DueRentDto> getDueRentReport(LocalDate month) {
        List<DueRentDto> report = new ArrayList<>();

        // Get all active tenants
        List<Tenant> activeTenants = tenantRepository.findByIsActiveTrue();

        for (Tenant tenant : activeTenants) {
            // Check if tenant has an active agreement
            Optional<RentAgreement> agreementOpt = rentAgreementRepository
                    .findByTenantIdAndIsActiveTrue(tenant.getId());

            if (agreementOpt.isPresent()) {
                RentAgreement agreement = agreementOpt.get();
                BigDecimal expectedAmount = agreement.getMonthlyRentAmount();

                // Get total paid for the month
                BigDecimal paidAmount = rentPaymentRepository
                        .sumAmountPaidByTenantIdAndPaymentForMonth(tenant.getId(), month);
                if (paidAmount == null) {
                    paidAmount = BigDecimal.ZERO;
                }

                BigDecimal dueAmount = expectedAmount.subtract(paidAmount);

                report.add(DueRentDto.builder()
                        .tenantId(tenant.getId())
                        .tenantName(tenant.getFullName())
                        .roomNumber(tenant.getRoom().getRoomNumber())
                        .expectedAmount(expectedAmount)
                        .paidAmount(paidAmount)
                        .dueAmount(dueAmount)
                        .month(month)
                        .build());
            }
        }

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentAgreement> getActiveAgreementByTenant(Long tenantId) {
        return rentAgreementRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentPayment> getPaymentsByTenant(Long tenantId) {
        return rentPaymentRepository.findByTenantIdOrderByPaymentDateDesc(tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentPayment> getPaymentsByMonth(LocalDate month) {
        return rentPaymentRepository.findByPaymentForMonth(month);
    }
}
