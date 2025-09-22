package com.ai.chat.controller;

import com.ai.chat.dto.RagRequest;
import com.ai.chat.entity.Document;
import com.ai.chat.service.DocumentService;
import com.ai.chat.service.RagService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
@Slf4j
public class RagController {
    private final RagService ragService;
    private final DocumentService documentService;

    public RagController(RagService ragService, DocumentService documentService) {
        this.ragService = ragService;
        this.documentService = documentService;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamRagChat(@RequestBody RagRequest request) {
        return ragService.streamRagCompletion(
            request.getQuestion(), 
            request.getModel(), 
            request.getMaxResults()
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file) {
        Document document = documentService.processDocument(file);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("RAG service is running");
    }
}