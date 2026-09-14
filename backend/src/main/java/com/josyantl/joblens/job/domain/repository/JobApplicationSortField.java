package com.josyantl.joblens.job.domain.repository;

import java.util.Arrays;

public enum JobApplicationSortField {
    UPDATED_AT("updatedAt"),
    CREATED_AT("createdAt"),
    COMPANY("company"),
    POSITION("position");

    private final String apiValue;

    JobApplicationSortField(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public static JobApplicationSortField fromApiValue(String value) {
        return Arrays.stream(values())
                .filter(field -> field.apiValue.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported sort field: " + value));
    }
}
