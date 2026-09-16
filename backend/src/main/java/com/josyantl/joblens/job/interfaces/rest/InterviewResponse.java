package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.*;
import java.time.Instant;

public record InterviewResponse(Long id, Long applicationId, int round, InterviewType type,
        Instant startsAt, Instant endsAt, int durationMinutes, String contact, String meetingUrl,
        String location, InterviewStatus status, InterviewFeedback feedback, Instant createdAt,
        Instant updatedAt, Instant completedAt, long version) {
    public static InterviewResponse from(Interview interview) {
        var details = interview.getDetails();
        return new InterviewResponse(interview.getId(), interview.getApplicationId(), details.round(),
                details.type(), details.startsAt(), details.endsAt(), details.durationMinutes(),
                details.contact(), details.meetingUrl(), details.location(), interview.getStatus(),
                interview.getFeedback(), interview.getCreatedAt(), interview.getUpdatedAt(),
                interview.getCompletedAt(), interview.getVersion());
    }
}
