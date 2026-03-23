package com.pimentadesenvolvimento.repository;

import com.pimentadesenvolvimento.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByPersonId(Long personId);
}
