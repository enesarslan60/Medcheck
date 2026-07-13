package com.drugchecker.dto;

import lombok.Data;

import java.util.List;

@Data
public class InteractionCheckRequest {

    private List<String> drugNames;
}
