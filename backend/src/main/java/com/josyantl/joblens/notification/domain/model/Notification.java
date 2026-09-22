package com.josyantl.joblens.notification.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification {
    private Long id;
    private Long userId;
    private Long applicationId;
    private NotificationType type;
    private NotificationSourceType sourceType;
    private Long sourceId;
    private String title;
    private String message;
    private Instant eventAt;
    private Instant createdAt;
    private Instant readAt;
    private String deduplicationKey;

    public static Notification create(Long userId, Long applicationId, NotificationType type,
            NotificationSourceType sourceType, Long sourceId, String title, String message,
            Instant eventAt, String deduplicationKey, Instant now) {
        return new Notification(null, positive(userId, "User"), positive(applicationId, "Application"),
                required(type, "Type"), required(sourceType, "Source type"), positive(sourceId, "Source"),
                text(title, 200, "Title"), text(message, 500, "Message"),
                required(eventAt, "Event time"), required(now, "Created time"), null,
                text(deduplicationKey, 200, "Deduplication key"));
    }

    public static Notification restore(Long id, Long userId, Long applicationId, NotificationType type,
            NotificationSourceType sourceType, Long sourceId, String title, String message,
            Instant eventAt, Instant createdAt, Instant readAt, String deduplicationKey) {
        Notification notification = create(userId, applicationId, type, sourceType, sourceId,
                title, message, eventAt, deduplicationKey, createdAt);
        notification.id = positive(id, "Notification");
        notification.readAt = readAt;
        return notification;
    }

    public void markRead(Instant now) {
        if (readAt == null) readAt = required(now, "Read time");
    }

    public boolean isRead() {
        return readAt != null;
    }

    private static Long positive(Long value, String label) {
        if (value == null || value <= 0) throw new IllegalArgumentException(label + " id must be positive");
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

