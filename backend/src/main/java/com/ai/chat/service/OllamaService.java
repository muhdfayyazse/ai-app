package com.ai.chat.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.ai.chat.dto.AiChatChoice;
import com.ai.chat.dto.AiChatMessage;
import com.ai.chat.dto.AiChatResponse;
import com.ai.chat.exception.OpenAIServiceException;
import com.ai.chat.helper.JsonHelper;
import com.ai.chat.tools.CurrentDateTimeTool;

import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spring.ai.ollama.enabled", havingValue = "true", matchIfMissing = false)
public class OllamaService implements AiService {

    private final JsonHelper jsonHelper;
    private final CurrentDateTimeTool currentDateTimeTool;

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String defaultBaseUrl;

    @Value("${spring.ai.ollama.chat.options.model:llama3.2:3b}")
    private String defaultModel;

    @Value("${spring.ai.ollama.chat.options.temperature:0.7}")
    private Double defaultTemperature;

    @Value("${spring.ai.ollama.chat.options.max-tokens:1000}")
    private Integer defaultMaxTokens;

    private final ToolCallingManager toolCallingManager;

    /**
     * Generate streaming chat completion using Spring AI Ollama
     */
    @Override
    public Flux<String> streamChatCompletion(String message) {
        try {
            log.info("Starting OpenAI streaming response for message: {}", currentDateTimeTool.getCurrentDateTime());

            OllamaApi ollamaApi = OllamaApi.builder()
                    .baseUrl(defaultBaseUrl)
                    .restClientBuilder(RestClient.builder())
                    .webClientBuilder(WebClient.builder())
                    .responseErrorHandler(new DefaultResponseErrorHandler())
                    .build();

            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model(defaultModel)
                    .temperature(defaultTemperature)
                    .numPredict(500)
                    .build();

            OllamaChatModel chatModel = new OllamaChatModel(
                    ollamaApi,
                    options,
                    toolCallingManager,
                    ObservationRegistry.create(),
                    ModelManagementOptions.builder().build());

            List<Message> messages = createMessageList(message);
            Prompt prompt = new Prompt(messages);

            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultTools(currentDateTimeTool)
                    .defaultAdvisors(new SimpleLoggerAdvisor())
                    .build();

            return chatClient.prompt(prompt).stream().chatResponse()
                    .map(this::createStandardResponse)
                    .doOnComplete(() -> log.info("Ollama streaming completed"))
                    .doOnError(error -> log.error("Error in Ollama streaming", error));

        } catch (Exception e) {
            log.error("Error starting Ollama streaming", e);
            return Flux.error(new OpenAIServiceException("Failed to start Ollama streaming: " + e.getMessage(), e));
        }
    }

    @Override
    public Flux<String> streamChatCompletion(List<Message> messages) {
        try {
            log.info("Starting OpenAI streaming response for message: {}", Arrays.toString(messages.toArray()));

            OllamaApi ollamaApi = OllamaApi.builder()
                    .baseUrl("http://localhost:11434")
                    .restClientBuilder(RestClient.builder())
                    .webClientBuilder(WebClient.builder())
                    .responseErrorHandler(new DefaultResponseErrorHandler())
                    .build();

            OllamaChatOptions options = OllamaChatOptions.builder()
                    .model(defaultModel)
                    .temperature(defaultTemperature)
                    .numPredict(500)
                    .build();

            OllamaChatModel chatModel = new OllamaChatModel(
                    ollamaApi,
                    options,
                    toolCallingManager,
                    ObservationRegistry.create(),
                    ModelManagementOptions.builder().build());

            Prompt prompt = new Prompt(messages);
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultTools(currentDateTimeTool)
                    .defaultAdvisors(new SimpleLoggerAdvisor())
                    .build();

            return chatClient.prompt(prompt).stream().chatResponse()
                    .map(this::createStandardResponse)
                    .doOnComplete(() -> log.info("Ollama streaming completed"))
                    .doOnError(error -> log.error("Error in Ollama streaming", error));

        } catch (Exception e) {
            log.error("Error starting Ollama streaming", e);
            return Flux.error(new OpenAIServiceException("Failed to start Ollama streaming: " + e.getMessage(), e));
        }
    }

    /**
     * Create message list with system and user messages
     */
    private List<Message> createMessageList(String userMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("You are a helpful assistant"));
        messages.add(new UserMessage(userMessage));
        return messages;
    }

    /**
     * Create standard OpenAI API format response
     */
    @Override
    public String createStandardResponse(ChatResponse response) {
        AiChatResponse aiChatResponse = new AiChatResponse();

        // Safe metadata access
        if (response.getMetadata() != null) {
            aiChatResponse.setId(response.getMetadata().getId());
            aiChatResponse.setModel(response.getMetadata().getModel());
        } else {
            aiChatResponse.setId("ollama-" + System.currentTimeMillis());
            aiChatResponse.setModel(defaultModel);
        }

        List<AiChatChoice> aiChatChoices = response.getResults().stream()
                .filter(result -> result.getOutput() != null)
                .map(result -> {
                    AiChatChoice aiChatChoice = new AiChatChoice();

                    AiChatMessage aiChatMessage = new AiChatMessage();

                    // Safe role access
                    String role = "assistant";
                    if (result.getOutput().getMetadata() != null
                            && result.getOutput().getMetadata().get("role") != null) {
                        role = result.getOutput().getMetadata().get("role").toString();
                    }
                    aiChatMessage.setRole(role);

                    // Safe content access - use getText() instead of getContent()
                    String content = "";
                    try {
                        content = result.getOutput().getText();
                    } catch (Exception e) {
                        log.warn("Could not get text content from result", e);
                        content = "";
                    }
                    aiChatMessage.setContent(content);

                    // Safe index access
                    String index = "0";
                    if (result.getOutput().getMetadata() != null
                            && result.getOutput().getMetadata().get("index") != null) {
                        index = result.getOutput().getMetadata().get("index").toString();
                    }
                    aiChatChoice.setIndex(index);

                    aiChatChoice.setMessage(aiChatMessage);

                    // Safe finish reason access
                    String finishReason = "";
                    if (result.getMetadata() != null && result.getMetadata().getFinishReason() != null) {
                        finishReason = result.getMetadata().getFinishReason().toString();
                    }
                    aiChatChoice.setFinishReason(finishReason);

                    return aiChatChoice;
                })
                .toList();
        aiChatResponse.setChoices(aiChatChoices);
        return jsonHelper.toJson(aiChatResponse);
    }
}