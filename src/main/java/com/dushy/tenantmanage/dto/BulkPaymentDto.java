package com.dushy.tenantmanage.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO for bulk payment recording.
 */
public class BulkPaymentDto {

    @NotNull(message = "Payments list is required")
    @Valid
    private List<PaymentEntry> payments;

    public BulkPaymentDto() {
    }

    public BulkPaymentDto(List<PaymentEntry> payments) {
        this.payments = payments;
    }

    // Getters and Setters
    public List<PaymentEntry> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentEntry> payments) {
        this.payments = payments;
    }

    /**
     * Inner class representing a single payment entry in a bulk operation.
     */
    public static class PaymentEntry {
        @NotNull(message = "Tenant ID is required")
        private Long tenantId;

        @NotNull(message = "Payment details are required")
        @Valid
        private RentPaymentDto payment;

        public PaymentEntry() {
        }

        public PaymentEntry(Long tenantId, RentPaymentDto payment) {
            this.tenantId = tenantId;
            this.payment = payment;
        }

        // Getters and Setters
        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public RentPaymentDto getPayment() {
            return payment;
        }

        public void setPayment(RentPaymentDto payment) {
            this.payment = payment;
        }
    }
}
