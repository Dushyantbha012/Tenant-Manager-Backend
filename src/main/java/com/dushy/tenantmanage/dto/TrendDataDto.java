package com.dushy.tenantmanage.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for trend analytics data points.
 */
public class TrendDataDto {
    private LocalDate month;
    private BigDecimal value;
    private String label;

    public TrendDataDto() {
    }

    public TrendDataDto(LocalDate month, BigDecimal value) {
        this.month = month;
        this.value = value;
    }

    public TrendDataDto(LocalDate month, BigDecimal value, String label) {
        this.month = month;
        this.value = value;
        this.label = label;
    }

    // Getters and Setters
    public LocalDate getMonth() {
        return month;
    }

    public void setMonth(LocalDate month) {
        this.month = month;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
