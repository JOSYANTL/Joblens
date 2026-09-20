package com.josyantl.joblens.job.domain.model;

import com.josyantl.joblens.job.domain.exception.InterviewStateException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.Instant;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Interview {
    private Long id;
    private Long applicationId;
    private InterviewDetails details;
    private InterviewStatus status;
    private InterviewFeedback feedback;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private long version;

    public static Interview schedule(Long applicationId, InterviewDetails details, Instant now) {
        if (applicationId == null || applicationId <= 0 || details == null || now == null)
            throw new IllegalArgumentException("Valid application, details and timestamp are required");
        return new Interview(null, applicationId, details, InterviewStatus.SCHEDULED,
                InterviewFeedback.empty(), now, now, null, 0);
    }

    public static Interview restore(Long id, Long applicationId, InterviewDetails details,
            InterviewStatus status, InterviewFeedback feedback, Instant createdAt,
            Instant updatedAt, Instant completedAt, long version) {
        Interview interview = schedule(applicationId, details, createdAt);
        if (id == null || id <= 0 || status == null || feedback == null || updatedAt == null || version < 0
                || ((status == InterviewStatus.COMPLETED) != (completedAt != null)))
            throw new IllegalArgumentException("Invalid persisted interview");
        interview.id = id;
        interview.status = status;
        interview.feedback = feedback;
        interview.updatedAt = updatedAt;
        interview.completedAt = completedAt;
        interview.version = version;
        return interview;
    }

    public void reschedule(InterviewDetails details, Instant now) {
        if (status != InterviewStatus.SCHEDULED)
            throw new InterviewStateException("Only scheduled interviews can be edited or rescheduled");
        if (details == null || now == null) throw new IllegalArgumentException("Details and timestamp are required");
        if (this.details.equals(details)) return;
        this.details = details;
        updatedAt = now;
    }

    public void changeStatus(InterviewStatus target, Instant now) {
        if (target == null || now == null) throw new IllegalArgumentException("Status and timestamp are required");
        if (status == target) return;
        if (status != InterviewStatus.SCHEDULED || target == InterviewStatus.SCHEDULED)
            throw new InterviewStateException("Completed or cancelled interviews cannot change status");
        if (target == InterviewStatus.COMPLETED && details.startsAt().isAfter(now))
            throw new InterviewStateException("An interview cannot be completed before it starts");
        status = target;
        completedAt = target == InterviewStatus.COMPLETED ? now : null;
        updatedAt = now;
    }

    public void recordFeedback(InterviewFeedback feedback, Instant now) {
        if (status != InterviewStatus.COMPLETED)
            throw new InterviewStateException("Feedback can only be recorded for completed interviews");
        if (feedback == null || now == null) throw new IllegalArgumentException("Feedback and timestamp are required");
        if (this.feedback.equals(feedback)) return;
        this.feedback = feedback;
        updatedAt = now;
    }
}
