package com.croi.knowledge.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {
    private final int chunkSize;
    private final int overlap;

    public TextChunker(@Value("${rag.chunk-size:1000}") int chunkSize,
                       @Value("${rag.chunk-overlap:200}") int overlap) {
        if (chunkSize <= overlap || overlap < 0) throw new IllegalArgumentException("Invalid RAG chunk configuration");
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    public List<String> chunk(String text) {
        String normalized = text == null ? "" : text.replaceAll("\\s+", " ").trim();
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < normalized.length(); start += chunkSize - overlap) {
            int end = Math.min(normalized.length(), start + chunkSize);
            if (end < normalized.length()) {
                int boundary = normalized.lastIndexOf(' ', end);
                if (boundary > start + chunkSize / 2) end = boundary;
            }
            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) chunks.add(chunk);
            if (end == normalized.length()) break;
        }
        return chunks;
    }
}
