package com.drugchecker.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class InteractionCheckRequest {

    @NotEmpty
    private List<String> drugNames;
}
