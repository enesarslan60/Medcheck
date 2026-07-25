package com.drugchecker.dto;

import java.util.List;

public class InteractionCheckRequest {

    private List<String> drugNames;

    public InteractionCheckRequest() {
    }

    public InteractionCheckRequest(List<String> drugNames) {
        this.drugNames = drugNames;
    }

    public List<String> getDrugNames() {
        return drugNames;
    }

    public void setDrugNames(List<String> drugNames) {
        this.drugNames = drugNames;
    }
}
