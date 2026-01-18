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
public class ChatSessionDto {
    private Long id;
    private Long userId;
    private String sessionToken;
    private String channel;
    private LocalDateTime startedAt;
    private LocalDateTime lastActivityAt;
    private Boolean isActive;
}
