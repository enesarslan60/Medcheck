package com.drugchecker.controller;

import com.drugchecker.dto.InteractionCheckRequest;
import com.drugchecker.dto.InteractionDTO;
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

    @PostMapping("/check")
    public ResponseEntity<List<InteractionDTO>> check(@RequestBody InteractionCheckRequest request) {
        validator.validateDrugNameList(request.getDrugNames());
        List<InteractionDTO> results = interactionService.checkInteractions(request.getDrugNames());
        return ResponseEntity.ok(results);
    }
}
