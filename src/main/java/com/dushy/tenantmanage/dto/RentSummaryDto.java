package com.dushy.tenantmanage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for monthly rent collection summary.
 */
public class RentSummaryDto {
    private LocalDate month;
    private BigDecimal expected;
    private BigDecimal collected;
    private BigDecimal pending;
    private double collectionRate;

    public RentSummaryDto() {
    }

    public RentSummaryDto(LocalDate month, BigDecimal expected, BigDecimal collected) {
        this.month = month;
        this.expected = expected;
        this.collected = collected;
        this.pending = expected.subtract(collected);
        this.collectionRate = expected.compareTo(BigDecimal.ZERO) > 0
                ? collected.doubleValue() / expected.doubleValue() * 100
                : 0;
    }

    // Getters and Setters
    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public BigDecimal getExpected() {
        return expected;
    }

    public void setExpected(BigDecimal expected) {
        this.expected = expected;
    }

    public BigDecimal getCollected() {
        return collected;
    }

    public void setCollected(BigDecimal collected) {
        this.collected = collected;
    }

    public BigDecimal getPending() {
        return pending;
    }

    public void setPending(BigDecimal pending) {
        this.pending = pending;
    }

    public double getCollectionRate() {
        return collectionRate;
    }

    public void setCollectionRate(double collectionRate) {
        this.collectionRate = collectionRate;
    }
}
