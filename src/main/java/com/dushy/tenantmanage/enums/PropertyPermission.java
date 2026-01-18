package com.dushy.tenantmanage.enums;

public enum PropertyPermission {
    VIEW_PROPERTY("View Property Details"),
    MANAGE_ROOMS("Manage Floors and Rooms"),
    MANAGE_TENANTS("Manage Tenants"),
    MANAGE_PAYMENTS("Record and Manage Payments"),
    VIEW_FINANCIALS("View Financial Reports"),
    MANAGE_SETTINGS("Manage Property Settings");

    private final String description;

    PropertyPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
