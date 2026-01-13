package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for AuditLog entity.
 * Manages system-wide activity tracking.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit history for a specific entity record.
     *
     * @param entityType the type of entity (e.g., "User", "Tenant")
     * @param entityId   the ID of the entity
     * @return list of audit log entries for the entity
     */
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

    /**
     * Find activity log for a specific user, ordered by most recent first.
     *
     * @param userId the ID of the user who made changes
     * @return list of audit log entries ordered by timestamp descending
     */
    List<AuditLog> findByChangedBy_IdOrderByTimestampDesc(Long userId);

    /**
     * Find all audit logs within a specific time range.
     * Used for time-based audit reports.
     *
     * @param start the start of the time range
     * @param end   the end of the time range
     * @return list of audit log entries within the time range
     */
    List<AuditLog> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
