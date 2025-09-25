package com.ai.chat.service;

import com.ai.chat.entity.Document;
import com.ai.chat.repository.DocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;

    public Document processDocument(MultipartFile file) {
        try {
            String content = extractTextFromFile(file);
            String originalName = file.getOriginalFilename();
            String safeName = originalName != null ? originalName : "unknown";
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

            Document document = Document.builder()
                .fileName(safeName)
                .fileType(contentType)
                .fileSize(file.getSize())
                .content(content)
                .build();
            return documentRepository.save(document);
        } catch (Exception e) {
            throw new RuntimeException("Error processing document: " + e.getMessage(), e);
        }
    }


    
    private String extractTextFromFile(MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename();
        String fileName = originalName == null ? "" : originalName.toLowerCase();
        
        if (fileName.endsWith(".pdf")) {
            return extractTextFromPdf(file);
        } else if (fileName.endsWith(".docx")) {
            return extractTextFromDocx(file);
        } else if (fileName.endsWith(".txt")) {
            return new String(file.getBytes());
        } else {
            throw new UnsupportedOperationException("Unsupported file type: " + fileName);
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }
}