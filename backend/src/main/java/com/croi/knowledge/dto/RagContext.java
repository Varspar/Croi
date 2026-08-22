package com.croi.knowledge.dto;

import java.util.List;

public record RagContext(String systemPrompt, List<RagSource> sources, double temperature, int maxTokens) {
}
