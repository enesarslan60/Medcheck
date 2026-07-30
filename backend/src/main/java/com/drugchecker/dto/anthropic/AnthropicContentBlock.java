package com.drugchecker.dto.anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One item in {@code response.content[]} — we only care about the "text" type. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnthropicContentBlock(String type, String text) {
}
