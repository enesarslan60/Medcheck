package com.drugchecker.controller;

import com.drugchecker.dto.InteractionCheckRequest;
import com.drugchecker.dto.InteractionDTO;
import com.drugchecker.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
@Validated
public class InteractionEndpoint {

    private final InteractionService interactionService;

    public InteractionEndpoint(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/check")
    public ResponseEntity<List<InteractionDTO>> check(@Valid @RequestBody InteractionCheckRequest request) {
        List<InteractionDTO> results = interactionService.checkInteractions(request.getDrugNames());
        return ResponseEntity.ok(results);
    }
}
