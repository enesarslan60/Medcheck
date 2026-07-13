package com.drugchecker.validation;

import com.drugchecker.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class DrugValidator {

    public static final int NAME_MIN = 2;
    public static final int NAME_MAX = 100;
    public static final int LIST_MIN = 2;
    public static final int LIST_MAX = 20;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9 .,'\\-]+$");

    public void validateDrugName(String name) {
        List<String> errors = new ArrayList<>();
        checkName(name, "name", errors);
        throwIfAny(errors);
    }

    public void validateDrugId(Long id) {
        List<String> errors = new ArrayList<>();
        if (id == null) {
            errors.add("id: must not be null");
        } else if (id < 1) {
            errors.add("id: must be >= 1");
        }
        throwIfAny(errors);
    }

    public void validateDrugNameList(List<String> names) {
        List<String> errors = new ArrayList<>();
        if (names == null || names.isEmpty()) {
            errors.add("drugNames: must not be empty");
            throwIfAny(errors);
            return;
        }
        if (names.size() < LIST_MIN) {
            errors.add("drugNames: must contain at least " + LIST_MIN + " entries");
        }
        if (names.size() > LIST_MAX) {
            errors.add("drugNames: must contain at most " + LIST_MAX + " entries");
        }
        for (int i = 0; i < names.size(); i++) {
            checkName(names.get(i), "drugNames[" + i + "]", errors);
        }
        throwIfAny(errors);
    }

    private void checkName(String name, String field, List<String> errors) {
        if (name == null || name.isBlank()) {
            errors.add(field + ": must not be blank");
            return;
        }
        String trimmed = name.trim();
        if (trimmed.length() < NAME_MIN || trimmed.length() > NAME_MAX) {
            errors.add(field + ": must be between " + NAME_MIN + " and " + NAME_MAX + " characters");
        }
        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            errors.add(field + ": contains invalid characters");
        }
    }

    private void throwIfAny(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
