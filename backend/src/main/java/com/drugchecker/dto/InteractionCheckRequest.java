package com.drugchecker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class InteractionCheckRequest {

    @NotEmpty(message = "drugNames must not be empty")
    @Size(min = 2, max = 20, message = "drugNames must contain between 2 and 20 entries")
    private List<
            @NotBlank(message = "drug name must not be blank")
            @Size(min = 2, max = 100, message = "drug name must be 2-100 characters")
            @Pattern(regexp = "^[A-Za-z0-9 .,'\\-]+$", message = "drug name contains invalid characters")
            String> drugNames;
}
