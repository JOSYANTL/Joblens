package com.josyantl.joblens.activity.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationNote {
    private Long id;
    private Long userId;
    private Long applicationId;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private long version;

    public static ApplicationNote create(Long userId, Long applicationId, String content, Instant now) {
        return new ApplicationNote(null, positive(userId), positive(applicationId), validContent(content),
                required(now), now, 0);
    }

    public static ApplicationNote restore(Long id, Long userId, Long applicationId, String content,
            Instant createdAt, Instant updatedAt, long version) {
        if (version < 0) throw new IllegalArgumentException("Version cannot be negative");
        return new ApplicationNote(positive(id), positive(userId), positive(applicationId),
                validContent(content), required(createdAt), required(updatedAt), version);
    }

    public void update(String content, Instant now) {
        this.content = validContent(content);
        this.updatedAt = required(now);
    }

    private static Long positive(Long value) {
        if (value == null || value <= 0) throw new IllegalArgumentException("Id must be positive");
        return value;
    }

    private static Instant required(Instant value) {
        if (value == null) throw new IllegalArgumentException("Timestamp is required");
        return value;
    }

    private static String validContent(String value) {
        if (value == null || value.isBlank() || value.length() > 5000)
            throw new IllegalArgumentException("Note must contain 1 to 5000 characters");
        return value.trim();
    }
}
