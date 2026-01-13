package com.dushy.tenantmanage.exception;

/**
 * Exception thrown when a business rule is violated.
 * Examples: Room already occupied, tenant already inactive, etc.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
