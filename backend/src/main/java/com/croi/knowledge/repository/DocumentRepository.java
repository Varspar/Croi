package com.croi.knowledge.repository;

import com.croi.knowledge.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByOrganizationId(UUID organizationId);

    List<Document> findByStatus(String status);
}
