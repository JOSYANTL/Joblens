package com.josyantl.joblens.shared.application;

import java.time.Instant;

public interface ApplicationActivityRecorder {
    void record(Long userId, Long applicationId, ApplicationActivityType type,
                ApplicationActivitySubjectType subjectType, Long subjectId,
                String summary, Instant occurredAt);
}
