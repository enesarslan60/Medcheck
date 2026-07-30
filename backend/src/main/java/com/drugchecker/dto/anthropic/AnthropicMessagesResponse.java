package com.drugchecker.dto.anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response body from POST /v1/messages */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnthropicMessagesResponse(
        String id,
        String model,
        String stop_reason,
        List<AnthropicContentBlock> content
) {
    /** Concatenates all text blocks — Claude usually returns exactly one. */
    public String firstText() {
        if (content == null || content.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (AnthropicContentBlock block : content) {
            if (block != null && "text".equals(block.type()) && block.text() != null) {
                sb.append(block.text());
            }
        }
        return sb.toString();
    }
}
