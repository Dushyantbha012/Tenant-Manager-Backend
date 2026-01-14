package com.dushy.tenantmanage.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't have
 * permission for.
 * Results in HTTP 403 Forbidden response.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String resourceType, Long resourceId) {
        super(String.format("Access denied to %s with id: %d", resourceType, resourceId));
    }

    public AccessDeniedException(String resourceType, Long resourceId, String action) {
        super(String.format("Access denied: cannot %s %s with id: %d", action, resourceType, resourceId));
    }
}
