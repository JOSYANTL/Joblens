package com.josyantl.joblens.identity.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

public record UserAccount(Long id, String email, String passwordHash, String displayName,
                          boolean enabled, Instant createdAt) {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public static UserAccount register(String email, String passwordHash, String displayName,
                                       Instant createdAt) {
        return new UserAccount(null, normalizeEmail(email), required(passwordHash, "Password hash"),
                validDisplayName(displayName), true, required(createdAt, "Created at"));
    }

    public UserAccount {
        if (id != null && id <= 0) throw new IllegalArgumentException("User id must be positive");
        email = normalizeEmail(email);
        passwordHash = required(passwordHash, "Password hash");
        displayName = validDisplayName(displayName);
        createdAt = required(createdAt, "Created at");
    }

    public static String normalizeEmail(String value) {
        String normalized = required(value, "Email").trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Email address is invalid");
        }
        return normalized;
    }

    private static String validDisplayName(String value) {
        String normalized = required(value, "Display name").trim();
        if (normalized.length() > 100) throw new IllegalArgumentException("Display name is too long");
        return normalized;
    }

    private static <T> T required(T value, String label) {
        if (value == null || value instanceof String text && text.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}
