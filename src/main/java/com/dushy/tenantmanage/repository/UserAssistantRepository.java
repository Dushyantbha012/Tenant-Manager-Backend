package com.dushy.tenantmanage.repository;

import com.dushy.tenantmanage.entity.UserAssistant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAssistantRepository extends JpaRepository<UserAssistant, Long> {
    List<UserAssistant> findByOwnerIdAndIsActiveTrue(Long ownerId);

    Optional<UserAssistant> findByOwnerIdAndAssistantId(Long ownerId, Long assistantId);
}
