# OpenAI Service Setup

This document explains how to set up and use the OpenAI service in your Spring Boot application.

## Prerequisites

1. **OpenAI API Key**: You need a valid OpenAI API key from [OpenAI Platform](https://platform.openai.com/api-keys)

## Configuration

### 1. Environment Variable
Set your OpenAI API key as an environment variable:
```bash
export OPENAI_API_KEY=your-actual-openai-api-key-here
```

### 2. Application Configuration
The application is configured in `application.yml`:
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your-openai-api-key-here}
      chat:
        options:
          model: gpt-3.5-turbo
          temperature: 0.7
          max-tokens: 1000
```

## Available Endpoints

### 1. Basic Chat Completion
**POST** `/api/ai/openai/chat`
```json
{
  "message": "Hello, how are you?",
  "model": "llama2"
}
```

### 2. Streaming Chat Completion
**POST** `/api/ai/openai/chat/stream`
```json
{
  "message": "Tell me a story",
  "model": "llama2"
}
```

### 3. Custom Chat Completion
**POST** `/api/ai/openai/chat/custom`
```json
{
  "message": "Explain quantum computing",
  "model": "gpt-4",
  "temperature": 0.3,
  "maxTokens": 500
}
```

### 4. Custom Streaming Chat Completion
**POST** `/api/ai/openai/chat/custom/stream`
```json
{
  "message": "Write a poem about spring",
  "model": "gpt-3.5-turbo",
  "temperature": 0.8,
  "maxTokens": 200
}
```

## OpenAIService Methods

The `OpenAIService` class provides the following methods:

### 1. `generateResponse(String message)`
Generates a single response using default settings.

### 2. `generateResponse(String message, String model, Double temperature, Integer maxTokens)`
Generates a response with custom parameters.

### 3. `generateResponseWithTemplate(String template, Map<String, Object> variables)`
Generates a response using a prompt template with variables.

### 4. `streamChatCompletion(String message)`
Streams responses using default settings.

### 5. `streamChatCompletion(String message, String model, Double temperature, Integer maxTokens)`
Streams responses with custom parameters.

### 6. `generateResponseWithHistory(List<Message> conversationHistory, String newMessage)`
Generates a response considering conversation history.

## Usage Examples

### Basic Usage
```java
@Autowired
private OpenAIService openAIService;

public String getResponse(String userMessage) {
    return openAIService.generateResponse(userMessage);
}
```

### Custom Parameters
```java
public String getCustomResponse(String userMessage) {
    return openAIService.generateResponse(
        userMessage, 
        "gpt-4", 
        0.5, 
        1000
    );
}
```

### Streaming
```java
public Flux<String> getStreamingResponse(String userMessage) {
    return openAIService.streamChatCompletion(userMessage);
}
```

## Error Handling

The service includes comprehensive error handling with custom exceptions:
- `OpenAIServiceException`: Custom exception for OpenAI service errors
- Proper logging for debugging
- Graceful error responses in controllers

## Testing

You can test the endpoints using curl:

```bash
# Basic chat
curl -X POST http://localhost:8080/api/ai/openai/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, how are you?"}'

# Streaming chat
curl -X POST http://localhost:8080/api/ai/openai/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Tell me a joke"}' \
  --no-buffer
```

## Notes

- Make sure your OpenAI API key has sufficient credits
- The service uses Spring AI framework for OpenAI integration
- All responses are logged for debugging purposes
- The service supports both synchronous and asynchronous (streaming) responses
