package com.drugchecker.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Gemini's system prompt wrapper — a {@code parts} list of text chunks. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiSystemInstruction(List<GeminiPart> parts) {
}
