package com.croi.knowledge.service;

import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import com.croi.common.exception.ValidationException;
import com.croi.knowledge.dto.DocumentDto;
import com.croi.knowledge.entity.Document;
import com.croi.knowledge.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentIngestionService ingestionService;

    public DocumentDto uploadDocument(UUID organizationId, String title, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Uploaded file must not be empty");
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";
        String fileType = extractFileType(fileName);
        if (!"pdf".equals(fileType)) {
            throw new ValidationException(ErrorCode.FILE_NOT_PDF, "Only PDF uploads are supported");
        }

        try {
            Document document = Document.builder()
                    .organizationId(organizationId).title(title).fileName(fileName).fileType(fileType)
                    .fileSize(file.getSize()).fileData(file.getBytes()).status("PROCESSING").build();
            document = documentRepository.save(document);
            ingestionService.ingest(document.getId());
            return toDto(document);
        } catch (java.io.IOException ex) {
            throw new ValidationException("Could not read uploaded PDF");
        }
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocument(UUID id) {
        return toDto(findDocumentOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> getDocumentsByOrganization(UUID organizationId) {
        return documentRepository.findByOrganizationId(organizationId).stream()
                .map(this::toDto)
                .toList();
    }

    public DocumentDto updateDocumentStatus(UUID id, String status) {
        Document document = findDocumentOrThrow(id);
        document.setStatus(status);
        return toDto(documentRepository.save(document));
    }

    public void deleteDocument(UUID id) {
        Document document = findDocumentOrThrow(id);
        documentRepository.delete(document);
    }

    public DocumentDto regenerateEmbeddings(UUID id) {
        Document document = findDocumentOrThrow(id);
        document.setStatus("PROCESSING");
        document.setErrorMessage(null);
        document = documentRepository.save(document);
        ingestionService.ingest(document.getId());
        return toDto(document);
    }

    private Document findDocumentOrThrow(UUID id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.DOCUMENT_NOT_FOUND, ErrorMessages.DOCUMENT_NOT_FOUND));
    }

    private String extractFileType(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "unknown";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private DocumentDto toDto(Document document) {
        return DocumentDto.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .title(document.getTitle())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .status(document.getStatus())
                .fileSize(document.getFileSize())
                .errorMessage(document.getErrorMessage())
                .chunkCount(document.getChunkCount())
                .embeddingCount(document.getEmbeddingCount())
                .createdAt(document.getCreatedAt())
                .build();
    }
}
