package com.drugchecker.controller;

import com.drugchecker.dto.InteractionCheckRequest;
import com.drugchecker.dto.InteractionExplanationResponse;
import com.drugchecker.service.InteractionService;
import com.drugchecker.validation.DrugValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Returns a top-level severity + LLM-generated German explanation of the
     * interaction between the selected drugs, plus per-drug details containing
     * the raw openFDA text and a short AI side-effect summary.
     * <p>
     * On LLM failure the response degrades gracefully: {@code severity=UNKNOWN},
     * {@code interactionSummary=null}, {@code llmAvailable=false}. Raw openFDA
     * data is still included so the frontend can render something useful.
     */
    @PostMapping("/check")
    public ResponseEntity<InteractionExplanationResponse> check(
            @RequestBody InteractionCheckRequest request) {
        validator.validateDrugNameList(request.getDrugNames());
        return ResponseEntity.ok(interactionService.checkInteractions(request.getDrugNames()));
    }
}
