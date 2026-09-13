package com.josyantl.joblens.job.domain.repository;

import java.util.Locale;

public enum SortDirection {
    ASC,
    DESC;

    public static SortDirection fromApiValue(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Unsupported sort direction: " + value);
        }
    }
}
