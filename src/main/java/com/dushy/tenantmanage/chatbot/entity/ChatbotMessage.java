package com.dushy.tenantmanage.chatbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(length = 10, nullable = false)
    private String direction; // 'INBOUND' or 'OUTBOUND'

    @Column(name = "message_text", nullable = false, columnDefinition = "TEXT")
    private String messageText;

    @Column(length = 100)
    private String intent; // detected intent

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence; // intent confidence score

    @Column(columnDefinition = "text")
    private String entities; // extracted entities as JSON

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
