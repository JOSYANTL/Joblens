package com.josyantl.joblens.job.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FollowUpTask {
    private Long id;
    private Long applicationId;
    private String title;
    private String notes;
    private Instant dueAt;
    private FollowUpTaskStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private long version;

    public static FollowUpTask create(Long applicationId, String title, String notes,
                                      Instant dueAt, Instant now) {
        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException("Application id must be positive");
        }
        return new FollowUpTask(null, applicationId, validTitle(title), validNotes(notes),
                required(dueAt), FollowUpTaskStatus.TODO, required(now), now, null, 0);
    }

    public static FollowUpTask restore(Long id, Long applicationId, String title, String notes,
            Instant dueAt, FollowUpTaskStatus status, Instant createdAt, Instant updatedAt,
            Instant completedAt, long version) {
        if (id == null || id <= 0 || version < 0 || status == null) {
            throw new IllegalArgumentException("Invalid persisted task");
        }
        FollowUpTask task = create(applicationId, title, notes, dueAt, createdAt);
        task.id = id;
        task.status = status;
        task.updatedAt = required(updatedAt);
        task.completedAt = completedAt;
        task.version = version;
        if ((status == FollowUpTaskStatus.DONE) != (completedAt != null)) {
            throw new IllegalArgumentException("Completed timestamp must match task status");
        }
        return task;
    }

    public void update(String title, String notes, Instant dueAt, Instant now) {
        String validTitle = validTitle(title);
        String validNotes = validNotes(notes);
        Instant validDueAt = required(dueAt);
        Instant validNow = required(now);
        this.title = validTitle;
        this.notes = validNotes;
        this.dueAt = validDueAt;
        this.updatedAt = validNow;
    }

    public void changeStatus(FollowUpTaskStatus status, Instant now) {
        if (status == null) throw new IllegalArgumentException("Status is required");
        Instant validNow = required(now);
        if (this.status == status) return;
        this.status = status;
        this.completedAt = status == FollowUpTaskStatus.DONE ? validNow : null;
        this.updatedAt = validNow;
    }

    public boolean isOverdue(Instant now) {
        return status == FollowUpTaskStatus.TODO && dueAt.isBefore(required(now));
    }

    private static String validTitle(String value) {
        if (value == null || value.isBlank() || value.length() > 200) {
            throw new IllegalArgumentException("Title must contain 1 to 200 characters");
        }
        return value.trim();
    }

    private static String validNotes(String value) {
        if (value != null && value.length() > 10000) {
            throw new IllegalArgumentException("Notes must not exceed 10000 characters");
        }
        return value == null ? "" : value;
    }

    private static Instant required(Instant value) {
        if (value == null) throw new IllegalArgumentException("Timestamp is required");
        return value;
    }
}
