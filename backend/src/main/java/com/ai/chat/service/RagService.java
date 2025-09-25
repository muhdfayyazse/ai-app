package com.ai.chat.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import com.ai.chat.dto.AiChatResponse;
import com.ai.chat.entity.Document;
import com.ai.chat.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagService {
    

    private final AiService aiService;

    private final DocumentRepository documentRepository;


    public Flux<String> streamRagCompletion(String question, int maxResults) {
        List<Document> relevantDocs = findRelevantDocuments(question, maxResults);
        String context = buildContext(relevantDocs);
        String enhancedPrompt = createEnhancedPrompt(question, context);
        return streamCompletion(enhancedPrompt);
    }

    private List<Document> findRelevantDocuments(String query, int maxResults) {
        List<Document> allDocs = documentRepository.findAll();
        
        if (allDocs.isEmpty()) return Collections.emptyList();

        Map<Document, Double> docScores = new HashMap<>();
        for (Document doc : allDocs) {
            double similarity = calculateSimilarity(query.toLowerCase(), 
                doc.getContent().toLowerCase());
            docScores.put(doc, similarity);
        }

        return docScores.entrySet().stream()
                .sorted(Map.Entry.<Document, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateSimilarity(String query, String content) {
        String[] queryWords = query.split("\\s+");
        String[] contentWords = content.split("\\s+");
        
        Set<String> uniqueWords = new HashSet<>();
        uniqueWords.addAll(Arrays.asList(queryWords));
        uniqueWords.addAll(Arrays.asList(contentWords));
        
        double[] queryVector = new double[uniqueWords.size()];
        double[] contentVector = new double[uniqueWords.size()];
        
        List<String> wordList = new ArrayList<>(uniqueWords);
        
        for (int i = 0; i < wordList.size(); i++) {
            String word = wordList.get(i);
            queryVector[i] = countOccurrences(queryWords, word);
            contentVector[i] = countOccurrences(contentWords, word);
        }
        
        return cosineSimilarity(queryVector, contentVector);
    }

    private double cosineSimilarity(double[] vectorA, double[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        
        return normA == 0 || normB == 0 ? 0 : dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private int countOccurrences(String[] words, String target) {
        int count = 0;
        for (String word : words) {
            if (word.equals(target)) count++;
        }
        return count;
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) return "No relevant documents found.";
        
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String contentPreview = doc.getContent().length() > 500 
                ? doc.getContent().substring(0, 500) + "..." 
                : doc.getContent();
            context.append("--- Document ").append(i + 1).append(": ")
                  .append(doc.getFileName()).append(" ---\n")
                  .append(contentPreview).append("\n\n");
        }
        return context.toString();
    }

    private String createEnhancedPrompt(String question, String context) {
        return String.format("""
            Based on the following documents, please answer the question. 
            If the information is not in the documents, say so.

            DOCUMENTS:
            %s

            QUESTION: %s

            Answer based only on the documents above:
            """, context, question);
    }

    private Flux<String> streamCompletion(String prompt) {
        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
                
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(escapedPrompt));    

        return aiService.streamChatCompletion(messages);

    }

   
}