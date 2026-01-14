package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.BulkPaymentDto;
import com.dushy.tenantmanage.dto.DueRentDto;
import com.dushy.tenantmanage.dto.RentPaymentDto;
import com.dushy.tenantmanage.dto.RentSummaryDto;
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

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userDetailsService.loadUserEntityByEmail(auth.getName());
    }

    // ==================== PAYMENT ENDPOINTS ====================

    @PostMapping("/payments/tenant/{tenantId}")
    public ResponseEntity<RentPayment> recordPayment(@PathVariable Long tenantId,
            @Valid @RequestBody RentPaymentDto paymentDto) {
        User currentUser = getCurrentUser();
        RentPayment payment = rentService.recordPayment(paymentDto, tenantId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/payments/tenant/{tenantId}")
    public ResponseEntity<List<RentPayment>> getPaymentsByTenant(@PathVariable Long tenantId) {
        List<RentPayment> payments = rentService.getPaymentsByTenant(tenantId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/month/{month}")
    public ResponseEntity<List<RentPayment>> getPaymentsByMonth(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        List<RentPayment> payments = rentService.getPaymentsByMonth(month);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/payments/search")
    public ResponseEntity<List<RentPayment>> searchPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<RentPayment> payments = rentService.searchPayments(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/payments/bulk")
    public ResponseEntity<List<RentPayment>> bulkRecordPayments(
            @Valid @RequestBody BulkPaymentDto bulkPaymentDto) {
        User currentUser = getCurrentUser();
        List<RentPayment> payments = rentService.bulkRecordPayments(bulkPaymentDto, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(payments);
    }

    // ==================== DUE RENT ENDPOINTS ====================

    @GetMapping("/due/{tenantId}")
    public ResponseEntity<DueRentDto> calculateDueRent(
            @PathVariable Long tenantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        LocalDate targetMonth = month != null ? month : LocalDate.now().withDayOfMonth(1);
        DueRentDto dueRent = rentService.calculateDueRent(tenantId, targetMonth);
        return ResponseEntity.ok(dueRent);
    }

    @GetMapping("/due/report")
    public ResponseEntity<List<DueRentDto>> getDueRentReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        LocalDate targetMonth = month != null ? month : LocalDate.now().withDayOfMonth(1);
        List<DueRentDto> report = rentService.getDueRentReport(targetMonth);
        return ResponseEntity.ok(report);
    }

    // ==================== SUMMARY ENDPOINTS ====================

    @GetMapping("/summary/property/{propertyId}")
    public ResponseEntity<RentSummaryDto> getRentSummary(@PathVariable Long propertyId) {
        RentSummaryDto summary = rentService.getRentSummaryByProperty(propertyId);
        return ResponseEntity.ok(summary);
    }

    // ==================== AGREEMENT ENDPOINTS ====================

    @GetMapping("/agreements/{tenantId}")
    public ResponseEntity<RentAgreement> getActiveAgreement(@PathVariable Long tenantId) {
        Optional<RentAgreement> agreement = rentService.getActiveAgreementByTenant(tenantId);
        return agreement.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
