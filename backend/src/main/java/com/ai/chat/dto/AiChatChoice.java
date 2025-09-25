package com.ai.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatChoice {
    private String index;
    private AiChatMessage message;
    private String finishReason;
}
