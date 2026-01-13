package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.DueRentDto;
import com.dushy.tenantmanage.dto.RentPaymentDto;
import com.dushy.tenantmanage.entity.RentAgreement;
import com.dushy.tenantmanage.entity.RentPayment;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.service.RentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for rent management.
 * Handles rent payments, agreements, and due rent calculations.
 */
@RestController
@RequestMapping("/api/rent")
public class RentController {

    private final RentService rentService;
    private final CustomUserDetailsService userDetailsService;

    public RentController(RentService rentService,
            CustomUserDetailsService userDetailsService) {
        this.rentService = rentService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Get the currently authenticated user.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    // ==================== PAYMENT ENDPOINTS ====================

    /**
     * Record a rent payment.
     *
     * @param tenantId   the tenant ID
     * @param paymentDto the payment data
     * @return the recorded payment
     */
    @PostMapping("/payments/tenant/{tenantId}")
    public ResponseEntity<RentPayment> recordPayment(@PathVariable Long tenantId,
            @Valid @RequestBody RentPaymentDto paymentDto) {
        User currentUser = getCurrentUser();
        RentPayment payment = rentService.recordPayment(paymentDto, tenantId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * Get all payments for a tenant.
     *
     * @param tenantId the tenant ID
     * @return list of payments
     */
    @GetMapping("/payments/tenant/{tenantId}")
    public ResponseEntity<List<RentPayment>> getPaymentsByTenant(@PathVariable Long tenantId) {
        List<RentPayment> payments = rentService.getPaymentsByTenant(tenantId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get all payments for a specific month.
     *
     * @param month the month (format: yyyy-MM-dd, typically first day of month)
     * @return list of payments
     */
    @GetMapping("/payments/month/{month}")
    public ResponseEntity<List<RentPayment>> getPaymentsByMonth(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        List<RentPayment> payments = rentService.getPaymentsByMonth(month);
        return ResponseEntity.ok(payments);
    }

    // ==================== DUE RENT ENDPOINTS ====================

    /**
     * Calculate due rent for a tenant for a specific month.
     *
     * @param tenantId the tenant ID
     * @param month    optional month (defaults to current month)
     * @return the due rent calculation
     */
    @GetMapping("/due/{tenantId}")
    public ResponseEntity<DueRentDto> calculateDueRent(
            @PathVariable Long tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        LocalDate targetMonth = month != null ? month : LocalDate.now().withDayOfMonth(1);
        DueRentDto dueRent = rentService.calculateDueRent(tenantId, targetMonth);
        return ResponseEntity.ok(dueRent);
    }

    /**
     * Get due rent report for all active tenants for a month.
     *
     * @param month optional month (defaults to current month)
     * @return list of due rent calculations
     */
    @GetMapping("/due/report")
    public ResponseEntity<List<DueRentDto>> getDueRentReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        LocalDate targetMonth = month != null ? month : LocalDate.now().withDayOfMonth(1);
        List<DueRentDto> report = rentService.getDueRentReport(targetMonth);
        return ResponseEntity.ok(report);
    }

    // ==================== AGREEMENT ENDPOINTS ====================

    /**
     * Get the active rent agreement for a tenant.
     *
     * @param tenantId the tenant ID
     * @return the active agreement or 204 No Content if none
     */
    @GetMapping("/agreements/{tenantId}")
    public ResponseEntity<RentAgreement> getActiveAgreement(@PathVariable Long tenantId) {
        Optional<RentAgreement> agreement = rentService.getActiveAgreementByTenant(tenantId);
        return agreement.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
