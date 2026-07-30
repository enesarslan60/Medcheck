package com.drugchecker.dto.anthropic;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Request body for POST https://api.anthropic.com/v1/messages */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnthropicMessagesRequest(
        String model,
        int max_tokens,
        String system,
        List<AnthropicMessage> messages
) {
}
