package com.ai.chat.service;

import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;

import com.ai.chat.dto.AiChatResponse;

import reactor.core.publisher.Flux;

public interface AiService {
    Flux<String> streamChatCompletion(String message);
    Flux<String> streamChatCompletion(List<Message> messages);
    String createStandardResponse(ChatResponse response);
}
