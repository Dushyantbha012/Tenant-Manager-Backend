package com.dushy.tenantmanage.chatbot.repository;

import com.dushy.tenantmanage.chatbot.entity.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {

    Optional<ChatbotSession> findBySessionToken(String sessionToken);

    Optional<ChatbotSession> findByUserIdAndIsActiveTrue(Long userId);

    List<ChatbotSession> findByUserIdOrderByStartedAtDesc(Long userId);
}
