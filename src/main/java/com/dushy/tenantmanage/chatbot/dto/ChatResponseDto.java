package com.dushy.tenantmanage.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDto {
    private String message; // Bot's response text
    private String sessionToken; // Session identifier
    private String intent; // Detected intent
    private Double confidence; // Intent confidence score
    private List<String> actions; // Suggested quick actions
    private Object data; // Any structured data (e.g., list of properties)
}
