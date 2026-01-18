package com.dushy.tenantmanage.chatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "session_token", unique = true, nullable = false)
    private String sessionToken;

    @Column(length = 50, nullable = false)
    private String channel; // 'WEB', 'WHATSAPP', 'TELEGRAM'

    @Column(name = "started_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime startedAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "text")
    private String metadata; // Store as JSON string
}
