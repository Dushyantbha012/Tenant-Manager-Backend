package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for rent payments with flattened fields.
 * Used for payment list displays where nested entity data is needed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentPaymentResponseDto {
    private Long id;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private LocalDate paymentForMonth;
    private PaymentMethod paymentMode;
    private String transactionReference;
    private String notes;

    // Tenant info (flattened)
    private Long tenantId;
    private String tenantName;

    // Property info (flattened)
    private Long propertyId;
    private String propertyName;

    // Room info (flattened)
    private Long roomId;
    private String roomNumber;
}
