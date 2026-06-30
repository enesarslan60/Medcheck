package com.drugchecker.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiError {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> details;
}
