package com.ai.chat.controller;

import com.ai.chat.dto.ChatRequest;
import com.ai.chat.service.OllamaService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
@Slf4j
public class ChatController {
    private final OllamaService ollamaService;

    public ChatController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequest request) {
        return ollamaService.streamChatCompletion(request.getMessage(), request.getModel());
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Chat service is running!";
    }
}