package com.ai.chat.repository;

import com.ai.chat.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByFileNameContainingIgnoreCase(String fileName);
}

