package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.*;
import org.springframework.stereotype.Component;

@Component
public class InterviewMapper {
    public Interview toDomain(InterviewJpaEntity entity) {
        return Interview.restore(entity.getId(), entity.getApplicationId(),
                new InterviewDetails(entity.getRound(), entity.getType(), entity.getStartsAt(),
                        entity.getDurationMinutes(), entity.getContact(), entity.getMeetingUrl(), entity.getLocation()),
                entity.getStatus(), new InterviewFeedback(entity.getQuestions(), entity.getSummary(), entity.getNextSteps()),
                entity.getCreatedAt(), entity.getUpdatedAt(), entity.getCompletedAt(), entity.getVersion());
    }

    public InterviewJpaEntity toEntity(Interview interview) {
        var entity = new InterviewJpaEntity();
        var details = interview.getDetails();
        var feedback = interview.getFeedback();
        entity.setId(interview.getId());
        entity.setApplicationId(interview.getApplicationId());
        entity.setRound(details.round());
        entity.setType(details.type());
        entity.setStartsAt(details.startsAt());
        entity.setDurationMinutes(details.durationMinutes());
        entity.setContact(details.contact());
        entity.setMeetingUrl(details.meetingUrl());
        entity.setLocation(details.location());
        entity.setStatus(interview.getStatus());
        entity.setQuestions(feedback.questions());
        entity.setSummary(feedback.summary());
        entity.setNextSteps(feedback.nextSteps());
        entity.setCreatedAt(interview.getCreatedAt());
        entity.setUpdatedAt(interview.getUpdatedAt());
        entity.setCompletedAt(interview.getCompletedAt());
        entity.setVersion(interview.getId() == null ? null : interview.getVersion());
        return entity;
    }
}
