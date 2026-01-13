# Repository Creation Plan

This document outlines the detailed plan for creating Spring Data JPA repositories for the Tenant Management System. Each repository will extend `JpaRepository` and include custom query methods to support business logic without requiring complex manual implementations.

## 1. Core Repositories

### 1.1 UserRepository
*   **Entity**: `User`
*   **Purpose**: Manage system users (Owners and Assistants).
*   **Key Custom Queries**:
    *   `findByEmail(String email)`: For authentication and login.
    *   `findByUserType(UserType userType)`: Filter users by role.
    *   `existsByEmail(String email)`: Check for existing users during registration.

### 1.2 PropertiesRepository
*   **Entity**: `Properties`
*   **Purpose**: Manage property-level data.
*   **Key Custom Queries**:
    *   `findByOwnerId(Long ownerId)`: List all properties owned by a specific user.
    *   `findByIsActiveTrue()`: List all active properties.
    *   `findByCity(String city)`: Search properties by location.

### 1.3 PropertyAccessRepository
*   **Entity**: `PropertyAccess`
*   **Purpose**: Manage assistant permissions for specific properties.
*   **Key Custom Queries**:
    *   `findByUserIdAndIsActiveTrue(Long userId)`: Find all properties a user has active access to.
    *   `findByPropertyIdAndIsActiveTrue(Long propertyId)`: Find all users with access to a property.
    *   `findByPropertyIdAndUserIdAndIsActiveTrue(Long propertyId, Long userId)`: Check specific access level.

## 2. Structural Repositories

### 2.1 FloorRepository
*   **Entity**: `Floor`
*   **Purpose**: Manage floors within properties.
*   **Key Custom Queries**:
    *   `findByPropertyIdOrderByFloorNumberAsc(Long propertyId)`: Get all floors for a property in order.
    *   `findByPropertyIdAndFloorNumber(Long propertyId, Integer floorNumber)`: Unique lookup for a floor.

### 2.2 RoomRepository
*   **Entity**: `Room`
*   **Purpose**: Manage individual rentable units.
*   **Key Custom Queries**:
    *   `findByFloorId(Long floorId)`: List all rooms on a specific floor.
    *   `findByIsOccupiedFalseAndIsActiveTrue()`: Find all available rooms across the system.
    *   `countByIsOccupiedTrueAndIsActiveTrue()`: Statistics for dashboard (total occupied).
    *   `findByFloorIdAndIsOccupiedFalse(Long floorId)`: Available rooms on a specific floor.

## 3. Operations Repositories

### 3.1 TenantRepository
*   **Entity**: `Tenant`
*   **Purpose**: Manage tenant profiles and occupancy.
*   **Key Custom Queries**:
    *   `findByRoomIdAndIsActiveTrue(Long roomId)`: Find the current tenant of a room.
    *   `findByIsActiveTrue()`: List all current active tenants.
    *   `findByPhone(String phone)`: Search tenant by contact.
    *   `findAllByMoveOutDateBefore(LocalDate date)`: History of moved-out tenants.

### 3.2 RentAgreementRepository
*   **Entity**: `RentAgreement`
*   **Purpose**: Manage legal and financial agreements.
*   **Key Custom Queries**:
    *   `findByTenantIdAndIsActiveTrue(Long tenantId)`: Get the currently active agreement for a tenant.
    *   `findAllByIsActiveTrue()`: List all active agreements for billing cycles.
    *   `findByEndDateBetween(LocalDate start, LocalDate end)`: Find agreements nearing expiration.

### 3.3 RentPaymentRepository
*   **Entity**: `RentPayment`
*   **Purpose**: Immutable log of all financial transactions.
*   **Key Custom Queries**:
    *   `findByTenantIdOrderByPaymentDateDesc(Long tenantId)`: Payment history for a specific tenant.
    *   `findByPaymentForMonth(LocalDate paymentMonth)`: Monthly collection report.
    *   `findByRentAgreementId(Long rentAgreementId)`: Payments against a specific agreement.
    *   `sumAmountPaidByTenantIdAndPaymentForMonth(Long tenantId, LocalDate month)`: Calculate total paid by tenant for a specific month.

## 4. Utility Repositories

### 4.1 AuditLogRepository
*   **Entity**: `AuditLog`
*   **Purpose**: System-wide activity tracking.
*   **Key Custom Queries**:
    *   `findByEntityTypeAndEntityId(String entityType, Long entityId)`: Audit history for a specific record.
    *   `findByChangedByOrderByTimestampDesc(Long userId)`: Activity log for a specific user.
    *   `findAllByTimestampBetween(LocalDateTime start, LocalDateTime end)`: Time-based audit reports.

## 5. Implementation Strategy
1.  Navigate to `com.dushy.tenantmanage.repository`.
2.  Create interfaces for each entity following the naming convention `[EntityName]Repository`.
3.  Annotate each with `@Repository`.
4.  Extend `JpaRepository<[Entity], Long>`.
5.  Add the custom query methods as specified above using Spring Data JPA's method naming convention.
