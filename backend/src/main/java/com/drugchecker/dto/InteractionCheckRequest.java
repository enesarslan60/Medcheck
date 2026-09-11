package com.drugchecker.dto;

import java.util.List;

public class InteractionCheckRequest {

    private List<String> drugNames;

    /** ISO 639-1 code of the desired response language (e.g. "de", "en"). Optional. */
    private String language;

    public InteractionCheckRequest() {
    }

    public InteractionCheckRequest(List<String> drugNames) {
        this.drugNames = drugNames;
    }

    public InteractionCheckRequest(List<String> drugNames, String language) {
        this.drugNames = drugNames;
        this.language = language;
    }

    public List<String> getDrugNames() {
        return drugNames;
    }

    public void setDrugNames(List<String> drugNames) {
        this.drugNames = drugNames;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
