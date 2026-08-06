package com.drugchecker.dto.ollama;

/** One message in the /api/chat conversation — role is "system", "user" or "assistant". */
public record OllamaChatMessage(String role, String content) {
}
