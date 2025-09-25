package com.ai.chat.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.chat.dto.AiChatResponse;
import com.ai.chat.dto.ChatRequest;
import com.ai.chat.service.AiService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
@AllArgsConstructor
@Slf4j
public class ChatController {
    
    //@Qualifier("openAIService")
    //@Qualifier("ollamaService")
    private final AiService aiService;


    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return aiService.streamChatCompletion(request.getMessage());
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Chat service is running!";
    }
}