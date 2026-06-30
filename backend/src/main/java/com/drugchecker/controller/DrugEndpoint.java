package com.drugchecker.controller;

import com.drugchecker.dto.DrugDTO;
import com.drugchecker.exception.ResourceNotFoundException;
import com.drugchecker.service.DrugService;
import com.drugchecker.service.OpenFDAService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drugs")
@Validated
public class DrugEndpoint {

    private static final int MAX_QUERY_LENGTH = 100;

    private final DrugService drugService;
    private final OpenFDAService openFDAService;

    public DrugEndpoint(DrugService drugService, OpenFDAService openFDAService) {
        this.drugService = drugService;
        this.openFDAService = openFDAService;
    }

    @GetMapping
    public ResponseEntity<List<DrugDTO>> getAll() {
        return ResponseEntity.ok(drugService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrugDTO> getById(@PathVariable @Min(1) Long id) {
        return ResponseEntity.ok(drugService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DrugDTO>> search(
            @RequestParam
            @NotBlank(message = "must not be blank")
            @Size(min = 2, max = MAX_QUERY_LENGTH,
                    message = "must be between 2 and " + MAX_QUERY_LENGTH + " characters")
            @Pattern(regexp = "^[A-Za-z0-9 .,'\\-]+$",
                    message = "contains invalid characters")
            String name) {
        return ResponseEntity.ok(drugService.searchByName(name.trim()));
    }

    @GetMapping("/openfda")
    public ResponseEntity<Map<String, Object>> openFda(
            @RequestParam
            @NotBlank(message = "must not be blank")
            @Size(min = 2, max = MAX_QUERY_LENGTH,
                    message = "must be between 2 and " + MAX_QUERY_LENGTH + " characters")
            @Pattern(regexp = "^[A-Za-z0-9 .,'\\-]+$",
                    message = "contains invalid characters")
            String name) {
        Map<String, Object> info = openFDAService.searchDrugInfo(name.trim());
        if (info == null || info.isEmpty()) {
            throw new ResourceNotFoundException("No OpenFDA data found for drug: " + name);
        }
        return ResponseEntity.ok(info);
    }
}
