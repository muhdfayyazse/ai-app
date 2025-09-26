package com.ai.chat.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.retry.support.RetryTemplate;
import io.micrometer.observation.ObservationRegistry;

@Configuration
public class AIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens}")
    private Integer maxTokens;

    @Bean
    public ToolCallingManager toolCallingManager() {
        return ToolCallingManager.builder().build();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder().build();
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        return ObservationRegistry.create();
    }

    @Bean
    public ChatModel chatModel(ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
        // Create OpenAiApi with all required parameters
        SimpleApiKey openAiApiKey = new SimpleApiKey(apiKey);

        OpenAiApi openAiApi = new OpenAiApi(
            "https://api.openai.com",
            openAiApiKey, // ApiKey - can be null
            new LinkedMultiValueMap<>(),
            "/v1/chat/completions",
            "/v1/embeddings",
            RestClient.builder(),
            WebClient.builder(),
            new DefaultResponseErrorHandler()
        );
        
        // Create OpenAiChatOptions
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
        
        // Create OpenAiChatModel with all required parameters
        return new OpenAiChatModel(
            openAiApi, 
            options, 
            toolCallingManager, // Now providing the required ToolCallingManager
            retryTemplate, // Now providing the required RetryTemplate
            observationRegistry  // Now providing the required ObservationRegistry
        );
    }
}