package com.drugchecker.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One part of a Gemini content — for our purposes just a text chunk. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiPart(String text) {
}
