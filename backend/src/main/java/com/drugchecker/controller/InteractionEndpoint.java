package com.drugchecker.controller;

import com.drugchecker.dto.DrugInteractionData;
import com.drugchecker.dto.InteractionCheckRequest;
import com.drugchecker.service.InteractionService;
import com.drugchecker.validation.DrugValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * Returns one entry per requested drug with its openFDA drug_interactions
     * text (from RxNorm-resolved cache or a fresh openFDA fetch).
     * The LLM per-pair analysis is not part of this endpoint.
     */
    @PostMapping("/check")
    public ResponseEntity<List<DrugInteractionData>> check(@RequestBody InteractionCheckRequest request) {
        validator.validateDrugNameList(request.getDrugNames());
        return ResponseEntity.ok(interactionService.checkInteractions(request.getDrugNames()));
    }
}
