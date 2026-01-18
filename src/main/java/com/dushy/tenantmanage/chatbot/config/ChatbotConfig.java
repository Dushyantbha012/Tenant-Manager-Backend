package com.dushy.tenantmanage.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConfigurationProperties(prefix = "chatbot")
@Data
public class ChatbotConfig {

    // Groq API configuration
    private String groqApiKey = System.getenv("GROQ_API_KEY");
    private String groqApiUrl = "https://api.groq.com/openai/v1/chat/completions";
    private String groqModel = "llama-3.3-70b-versatile";
    private Integer groqMaxTokens = 1000;
    private Double groqTemperature = 0.7;

    // Session configuration
    private Integer sessionTimeoutMinutes = 60; // 1 hour
    private Integer maxMessagesPerSession = 100;

    // Intent recognition configuration
    private Double intentConfidenceThreshold = 0.7;

    @Bean
    public WebClient groqWebClient() {
        return WebClient.builder()
                .baseUrl(groqApiUrl)
                .defaultHeader("Authorization", "Bearer " + groqApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
