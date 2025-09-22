package com.ai.chat.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class OllamaService {
    private final WebClient webClient;

    public OllamaService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Flux<String> streamChatCompletion(String message, String model) {
        String escapedMessage = message
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
                
        String requestBody = String.format("""
            {
                "model": "%s",
                "messages": [
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ],
                "stream": true
            }
        """, model, escapedMessage);

        return webClient.post()
                .uri("/api/chat")
                .bodyValue(requestBody)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
                // .filter(response -> response != null && !response.trim().isEmpty())
                // .map(this::extractContent)
                // .filter(content -> !content.isEmpty());
    }

    private String extractContent(String responseLine) {
        try {
            log.debug("Received line: {}", responseLine);
            String line = responseLine.trim();
            
            // Handle SSE format
            if (line.startsWith("data: ")) {
                line = line.substring(6).trim();
            }
            
            if ("[DONE]".equals(line)) {
                return "[DONE]";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(line);
            
            // Ollama chat stream format: { "message": { "role": "assistant", "content": "..." }, "done": false }
            JsonNode message = root.get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null && !content.isNull()) {
                    String text = content.asText();
                    log.debug("Extracted content: {}", text);
                    return text;
                }
            }
            
            // Check if stream is done
            JsonNode done = root.get("done");
            if (done != null && done.asBoolean(false)) {
                return "[DONE]";
            }
            
            return "";
        } catch (Exception e) {
            log.warn("Failed to parse stream line: {}", responseLine, e);
            return "";
        }
    }
}


