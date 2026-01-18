package com.dushy.tenantmanage.chatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "action_type", length = 100, nullable = false)
    private String actionType; // 'VIEW_PROPERTIES', 'RECORD_PAYMENT', etc.

    @Column(name = "entity_type", length = 50)
    private String entityType; // 'PROPERTY', 'TENANT', 'PAYMENT'

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "request_payload", columnDefinition = "text")
    private String requestPayload;

    @Column(name = "response_status", length = 50)
    private String responseStatus; // 'SUCCESS', 'FAILED', 'PENDING_CONFIRMATION'

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
