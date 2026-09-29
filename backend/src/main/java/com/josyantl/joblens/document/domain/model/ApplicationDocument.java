package com.josyantl.joblens.document.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationDocument {
    public static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private Long id;
    private Long userId;
    private Long applicationId;
    private ApplicationDocumentType type;
    private String originalFileName;
    private String contentType;
    private long sizeBytes;
    private String storageKey;
    private Instant createdAt;

    public static ApplicationDocument create(Long userId, Long applicationId,
            ApplicationDocumentType type, String originalFileName, String contentType,
            long sizeBytes, String storageKey, Instant createdAt) {
        return new ApplicationDocument(null, positive(userId, "User"),
                positive(applicationId, "Application"), required(type, "Document type"),
                text(originalFileName, 255, "File name"), text(contentType, 120, "Content type"),
                validSize(sizeBytes), text(storageKey, 100, "Storage key"),
                required(createdAt, "Created time"));
    }

    public static ApplicationDocument restore(Long id, Long userId, Long applicationId,
            ApplicationDocumentType type, String originalFileName, String contentType,
            long sizeBytes, String storageKey, Instant createdAt) {
        ApplicationDocument document = create(userId, applicationId, type, originalFileName,
                contentType, sizeBytes, storageKey, createdAt);
        document.id = positive(id, "Document");
        return document;
    }

    private static Long positive(Long value, String label) {
        if (value == null || value <= 0) throw new IllegalArgumentException(label + " id must be positive");
        return value;
    }

    private static long validSize(long value) {
        if (value <= 0 || value > MAX_SIZE_BYTES)
            throw new IllegalArgumentException("File size must be between 1 byte and 10 MB");
        return value;
    }

    private static String text(String value, int max, String label) {
        if (value == null || value.isBlank() || value.length() > max)
            throw new IllegalArgumentException(label + " must contain 1 to " + max + " characters");
        return value.trim();
    }

    private static <T> T required(T value, String label) {
        if (value == null) throw new IllegalArgumentException(label + " is required");
        return value;
    }
}
