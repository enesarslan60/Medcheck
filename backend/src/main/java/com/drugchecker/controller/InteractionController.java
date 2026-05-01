package com.drugchecker.controller;

import com.drugchecker.dto.InteractionCheckRequest;
import com.drugchecker.dto.InteractionDTO;
import com.drugchecker.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/check")
    public List<InteractionDTO> check(@Valid @RequestBody InteractionCheckRequest request) {
        return interactionService.checkInteractions(request.getDrugNames());
    }
}
