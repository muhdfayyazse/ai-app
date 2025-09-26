package com.ai.chat.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CurrentDateTimeTool {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("EEEE, MMMM d, yyyy 'at' hh:mm:ss a z");

    /**
     * Tool to retrieve the current date and time in ISO format.
     * The model will call this when asked about the current time.
     *
     * @return A formatted string representing the current date and time.
     */
    @Tool(description = "Get the current date and time in a detailed format, including the day of the week, for accurate and realtime information. Use this tool when users ask about the current time, date, what time is it, or when they need current timestamp information.")
    public String getCurrentDateTime() {
        // Return the current date and time with a specific format and system timezone
        return LocalDateTime.now().toString();
    }
}
