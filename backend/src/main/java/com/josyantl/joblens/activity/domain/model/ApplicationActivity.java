package com.josyantl.joblens.activity.domain.model;

import com.josyantl.joblens.shared.application.ApplicationActivitySubjectType;
import com.josyantl.joblens.shared.application.ApplicationActivityType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationActivity {
    private Long id;
    private Long userId;
    private Long applicationId;
    private ApplicationActivityType type;
    private ApplicationActivitySubjectType subjectType;
    private Long subjectId;
    private String summary;
    private Instant occurredAt;

    public static ApplicationActivity create(Long userId, Long applicationId,
            ApplicationActivityType type, ApplicationActivitySubjectType subjectType,
            Long subjectId, String summary, Instant occurredAt) {
        return new ApplicationActivity(null, positive(userId), positive(applicationId), required(type),
                required(subjectType), subjectId, validSummary(summary), required(occurredAt));
    }

    public static ApplicationActivity restore(Long id, Long userId, Long applicationId,
            ApplicationActivityType type, ApplicationActivitySubjectType subjectType,
            Long subjectId, String summary, Instant occurredAt) {
        ApplicationActivity activity = create(userId, applicationId, type, subjectType,
                subjectId, summary, occurredAt);
        activity.id = positive(id);
        return activity;
    }

    private static Long positive(Long value) {
        if (value == null || value <= 0) throw new IllegalArgumentException("Id must be positive");
        return value;
    }

    private static <T> T required(T value) {
        if (value == null) throw new IllegalArgumentException("Required value is missing");
        return value;
    }

    private static String validSummary(String value) {
        if (value == null || value.isBlank() || value.length() > 500)
            throw new IllegalArgumentException("Activity summary must contain 1 to 500 characters");
        return value.trim();
    }
}
