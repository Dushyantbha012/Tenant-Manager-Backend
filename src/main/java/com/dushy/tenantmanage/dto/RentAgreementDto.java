package com.dushy.tenantmanage.dto;

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
public class RentAgreementDto {
    private BigDecimal monthlyRentAmount;
    private BigDecimal securityDeposit;
    private LocalDate startDate;
    private Integer paymentDueDay;
}
