package com.dushy.tenantmanage.dto;

import com.dushy.tenantmanage.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentPaymentDto {
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private LocalDate paymentForMonth;
    private PaymentMethod paymentMode;
    private String transactionReference;
    private String notes;
}
