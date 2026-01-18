package com.dushy.tenantmanage.chatbot.repository;

import com.dushy.tenantmanage.chatbot.entity.ChatbotAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatbotActionRepository extends JpaRepository<ChatbotAction, Long> {

    List<ChatbotAction> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    List<ChatbotAction> findByActionTypeAndCreatedAtBetween(
            String actionType, LocalDateTime startDate, LocalDateTime endDate);
}
