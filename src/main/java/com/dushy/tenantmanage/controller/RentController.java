package com.dushy.tenantmanage.controller;

import com.dushy.tenantmanage.dto.BulkPaymentDto;
import com.dushy.tenantmanage.dto.DueRentDto;
import com.dushy.tenantmanage.dto.RentPaymentDto;
import com.dushy.tenantmanage.dto.RentPaymentResponseDto;
import com.dushy.tenantmanage.dto.RentSummaryDto;
import com.dushy.tenantmanage.entity.RentAgreement;
import com.dushy.tenantmanage.entity.RentPayment;
import com.dushy.tenantmanage.entity.User;
import com.dushy.tenantmanage.security.CustomUserDetailsService;
import com.dushy.tenantmanage.security.PropertyAuthorizationService;
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
 * All endpoints are secured with property-level authorization.
 */
@RestController
@RequestMapping("/api/rent")
public class RentController {

        private final RentService rentService;
        private final CustomUserDetailsService userDetailsService;
        private final PropertyAuthorizationService authorizationService;

        public RentController(RentService rentService,
                        CustomUserDetailsService userDetailsService,
                        PropertyAuthorizationService authorizationService) {
                this.rentService = rentService;
                this.userDetailsService = userDetailsService;
                this.authorizationService = authorizationService;
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
                // Require MANAGE_PAYMENTS permission
                Long propertyId = authorizationService.getPropertyIdFromTenant(tenantId);
                authorizationService.checkPropertyPermission(currentUser.getId(), propertyId,
                                com.dushy.tenantmanage.enums.PropertyPermission.MANAGE_PAYMENTS);

                RentPayment payment = rentService.recordPayment(paymentDto, tenantId, currentUser.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        }

        @GetMapping("/payments/tenant/{tenantId}")
        public ResponseEntity<List<RentPayment>> getPaymentsByTenant(@PathVariable Long tenantId) {
                User currentUser = getCurrentUser();
                // Check VIEW_FINANCIALS permission
                Long propertyId = authorizationService.getPropertyIdFromTenant(tenantId);
                authorizationService.checkPropertyPermission(currentUser.getId(), propertyId,
                                com.dushy.tenantmanage.enums.PropertyPermission.VIEW_FINANCIALS);

                List<RentPayment> payments = rentService.getPaymentsByTenant(tenantId);
                return ResponseEntity.ok(payments);
        }

        @GetMapping("/payments/month/{month}")
        public ResponseEntity<List<RentPayment>> getPaymentsByMonth(
                        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
                User currentUser = getCurrentUser();
                // Get all payments and filter by accessible properties
                List<RentPayment> allPayments = rentService.getPaymentsByMonth(month);

                List<RentPayment> accessiblePayments = allPayments.stream()
                                .filter(payment -> authorizationService.hasPropertyPermission(
                                                currentUser.getId(),
                                                payment.getTenant().getRoom().getFloor().getProperty().getId(),
                                                com.dushy.tenantmanage.enums.PropertyPermission.VIEW_FINANCIALS))
                                .toList();
                return ResponseEntity.ok(accessiblePayments);
        }

        @GetMapping("/payments/search")
        public ResponseEntity<List<RentPaymentResponseDto>> searchPayments(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false) Long propertyId,
                        @RequestParam(required = false) Long roomId) {
                User currentUser = getCurrentUser();

                // Get payments with filters
                List<RentPaymentResponseDto> allPayments = rentService.searchPaymentsWithFilters(startDate, endDate,
                                propertyId,
                                roomId);

                // Filter by accessible properties
                List<RentPaymentResponseDto> accessiblePayments = allPayments.stream()
                                .filter(payment -> authorizationService.hasPropertyPermission(
                                                currentUser.getId(),
                                                payment.getPropertyId(),
                                                com.dushy.tenantmanage.enums.PropertyPermission.VIEW_FINANCIALS))
                                .toList();
                return ResponseEntity.ok(accessiblePayments);
        }

        @PostMapping("/payments/bulk")
        public ResponseEntity<List<RentPayment>> bulkRecordPayments(
                        @Valid @RequestBody BulkPaymentDto bulkPaymentDto) {
                User currentUser = getCurrentUser();
                // Verify write access for all tenants in the bulk payment
                for (var paymentRecord : bulkPaymentDto.getPayments()) {
                        Long propertyId = authorizationService.getPropertyIdFromTenant(paymentRecord.getTenantId());
                        authorizationService.checkPropertyPermission(currentUser.getId(), propertyId,
                                        com.dushy.tenantmanage.enums.PropertyPermission.MANAGE_PAYMENTS);
                }

                List<RentPayment> payments = rentService.bulkRecordPayments(bulkPaymentDto, currentUser.getId());
                return ResponseEntity.status(HttpStatus.CREATED).body(payments);
        }

        // ==================== DUE RENT ENDPOINTS ====================

        @GetMapping("/due/{tenantId}")
        public ResponseEntity<DueRentDto> calculateDueRent(
                        @PathVariable Long tenantId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
                User currentUser = getCurrentUser();
                // Check VIEW_FINANCIALS permission
                Long propertyId = authorizationService.getPropertyIdFromTenant(tenantId);
                authorizationService.checkPropertyPermission(currentUser.getId(), propertyId,
                                com.dushy.tenantmanage.enums.PropertyPermission.VIEW_FINANCIALS);

                LocalDate targetMonth = month != null ? month : LocalDate.now().withDayOfMonth(1);
                DueRentDto dueRent = rentService.calculateDueRent(tenantId, targetMonth);
                return ResponseEntity.ok(dueRent);
        }

        @GetMapping("/due/report")
        public ResponseEntity<List<DueRentDto>> getDueRentReport(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
                User currentUser = getCurrentUser();
                LocalDate targetMonth = month != null ? month : LocalDate.now().withDayOfMonth(1);

                // Get all due rent and filter by accessible properties
                List<DueRentDto> allDueRent = rentService.getDueRentReport(targetMonth);

                List<DueRentDto> accessibleDueRent = allDueRent.stream()
                                .filter(due -> authorizationService.hasPropertyPermission(
                                                currentUser.getId(),
                                                due.getPropertyId(),
                                                com.dushy.tenantmanage.enums.PropertyPermission.VIEW_FINANCIALS))
                                .toList();
                return ResponseEntity.ok(accessibleDueRent);
        }

        // ==================== SUMMARY ENDPOINTS ====================

        @GetMapping("/summary/property/{propertyId}")
        public ResponseEntity<RentSummaryDto> getRentSummary(@PathVariable Long propertyId) {
                User currentUser = getCurrentUser();
                // Check VIEW_FINANCIALS permission
                authorizationService.checkPropertyPermission(currentUser.getId(), propertyId,
                                com.dushy.tenantmanage.enums.PropertyPermission.VIEW_FINANCIALS);

                RentSummaryDto summary = rentService.getRentSummaryByProperty(propertyId);
                return ResponseEntity.ok(summary);
        }

        // ==================== AGREEMENT ENDPOINTS ====================

        @GetMapping("/agreements/{tenantId}")
        public ResponseEntity<RentAgreement> getActiveAgreement(@PathVariable Long tenantId) {
                User currentUser = getCurrentUser();
                // Check access to tenant's property (Agreements are viewable with VIEW_PROPERTY
                // or VIEW_FINANCIALS?)
                // Let's say VIEW_PROPERTY is enough to see agreement details as it is part of
                // tenant management.
                Long propertyId = authorizationService.getPropertyIdFromTenant(tenantId);
                authorizationService.checkPropertyPermission(currentUser.getId(), propertyId,
                                com.dushy.tenantmanage.enums.PropertyPermission.VIEW_PROPERTY);

                Optional<RentAgreement> agreement = rentService.getActiveAgreementByTenant(tenantId);
                return agreement.map(ResponseEntity::ok)
                                .orElse(ResponseEntity.noContent().build());
        }
}
