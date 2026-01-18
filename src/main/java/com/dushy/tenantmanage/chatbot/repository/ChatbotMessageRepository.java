package com.dushy.tenantmanage.chatbot.repository;

import com.dushy.tenantmanage.chatbot.entity.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    List<ChatbotMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    long countBySessionId(Long sessionId);
}
