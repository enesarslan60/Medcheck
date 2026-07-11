package com.drugchecker.validation;

public final class ValidationRules {

    public static final int DRUG_NAME_MIN = 2;
    public static final int DRUG_NAME_MAX = 100;
    public static final String DRUG_NAME_PATTERN = "^[A-Za-z0-9 .,'\\-]+$";

    public static final int DRUG_LIST_MIN = 2;
    public static final int DRUG_LIST_MAX = 20;

    private ValidationRules() {
    }
}
