package com.drugchecker.controller;

import com.drugchecker.dto.InteractionCheckRequest;
import com.drugchecker.dto.InteractionExplanationResponse;
import com.drugchecker.model.SupportedLanguage;
import com.drugchecker.service.InteractionService;
import com.drugchecker.validation.DrugValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/interactions")
public class InteractionEndpoint {

    private final InteractionService interactionService;
    private final DrugValidator validator;

    public InteractionEndpoint(InteractionService interactionService, DrugValidator validator) {
        this.interactionService = interactionService;
        this.validator = validator;
    }

    /**
     * Returns a top-level severity + LLM-generated explanation of the
     * interaction between the selected drugs, plus per-drug details containing
     * the raw openFDA text and a short AI side-effect summary.
     *
     * <p>The output language of the LLM-generated fields is controlled by
     * {@link InteractionCheckRequest#getLanguage()} (ISO 639-1 code, e.g.
     * {@code "de"}, {@code "en"}, {@code "tr"}). Unknown or missing codes
     * fall back to {@link SupportedLanguage#DEFAULT}.
     *
     * <p>On LLM failure the response degrades gracefully:
     * {@code severity=UNKNOWN}, {@code interactionSummary=null},
     * {@code llmAvailable=false}. Raw openFDA data is still included so
     * the frontend can render something useful.
     */
    @PostMapping("/check")
    public ResponseEntity<InteractionExplanationResponse> check(
            @RequestBody InteractionCheckRequest request) {
        validator.validateDrugNameList(request.getDrugNames());
        SupportedLanguage lang = SupportedLanguage.fromCode(request.getLanguage());
        return ResponseEntity.ok(
                interactionService.checkInteractions(request.getDrugNames(), lang));
    }

    /**
     * Lists the languages the frontend may pick for LLM output.
     * Returned as {@code [ { "code": "de", "displayName": "Deutsch" }, ... ]}
     * so the UI can render a native-name selector without hard-coding the list.
     */
    @GetMapping("/languages")
    public List<LanguageOption> supportedLanguages() {
        return Arrays.stream(SupportedLanguage.values())
                .map(l -> new LanguageOption(l.getCode(), l.getDisplayName()))
                .toList();
    }

    public record LanguageOption(String code, String displayName) {
    }
}
