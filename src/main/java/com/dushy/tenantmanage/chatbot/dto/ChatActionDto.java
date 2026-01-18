package com.dushy.tenantmanage.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatActionDto {
    private Long id;
    private Long sessionId;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String responseStatus;
    private LocalDateTime createdAt;
}
