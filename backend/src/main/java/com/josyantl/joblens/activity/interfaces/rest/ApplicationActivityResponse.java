package com.josyantl.joblens.activity.interfaces.rest;

import com.josyantl.joblens.activity.domain.model.ApplicationActivity;

import java.time.Instant;

public record ApplicationActivityResponse(
        Long id,
        Long applicationId,
        String type,
        String subjectType,
        Long subjectId,
        String summary,
        Instant occurredAt
) {
    public static ApplicationActivityResponse from(ApplicationActivity value) {
        return new ApplicationActivityResponse(
                value.getId(),
                value.getApplicationId(),
                value.getType().name(),
                value.getSubjectType().name(),
                value.getSubjectId(),
                value.getSummary(),
                value.getOccurredAt()
        );
    }
}
