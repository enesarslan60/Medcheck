package com.drugchecker.dto.anthropic;

/** One message in a Claude conversation — role is "user" or "assistant". */
public record AnthropicMessage(String role, String content) {
}
