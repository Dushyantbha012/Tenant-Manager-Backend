package com.dushy.tenantmanage.entity;

import com.dushy.tenantmanage.enums.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Table(name = "rent_payments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rent_agreement_id", nullable = false)
    private RentAgreement rentAgreement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "payment_for_month", nullable = false)
    private LocalDate paymentForMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", length = 50)
    private PaymentMethod paymentMode;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;
}
