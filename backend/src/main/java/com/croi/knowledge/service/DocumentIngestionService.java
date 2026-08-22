package com.croi.knowledge.service;

import com.croi.knowledge.entity.Document;
import com.croi.knowledge.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {
    private final DocumentRepository documentRepository;
    private final OllamaEmbeddingService embeddingService;
    private final TextChunker textChunker;
    private final JdbcTemplate jdbcTemplate;

    @Async
    public void ingest(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) return;
        try {
            if (!"pdf".equalsIgnoreCase(document.getFileType())) {
                throw new IllegalArgumentException("Only PDF files are supported");
            }
            String text = extractText(document.getFileData());
            List<String> chunks = textChunker.chunk(text);
            if (chunks.isEmpty()) throw new IllegalArgumentException("The PDF contains no extractable text");

            jdbcTemplate.update("DELETE FROM document_embeddings WHERE document_id = ?", documentId);
            int index = 0;
            for (String chunk : chunks) {
                jdbcTemplate.update("INSERT INTO document_embeddings (id, document_id, chunk_index, content, embedding, created_at, updated_at) "
                                + "VALUES (gen_random_uuid(), ?, ?, ?, CAST(? AS vector), now(), now())",
                        documentId, index++, chunk, vectorLiteral(embeddingService.embed(chunk)));
            }
            document.setChunkCount(chunks.size());
            document.setEmbeddingCount(chunks.size());
            document.setErrorMessage(null);
            document.setStatus("READY");
            documentRepository.save(document);
        } catch (Exception ex) {
            log.error("Document ingestion failed for {}", documentId, ex);
            document.setStatus("FAILED");
            document.setErrorMessage(ex.getMessage());
            documentRepository.save(document);
        }
    }

    private String extractText(byte[] data) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(data)) {
            return new PDFTextStripper().getText(pdf);
        }
    }

    public static String vectorLiteral(float[] vector) {
        StringBuilder value = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) value.append(',');
            value.append(vector[i]);
        }
        return value.append(']').toString();
    }
}
