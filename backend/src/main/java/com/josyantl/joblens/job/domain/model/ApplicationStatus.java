package com.josyantl.joblens.job.domain.model;

import java.util.Set;

public enum ApplicationStatus {
    SAVED,
    APPLIED,
    INTERVIEW_SCHEDULED,
    OFFERED,
    REJECTED;

    public Set<ApplicationStatus> allowedTransitions() {
        return switch (this) {
            case SAVED -> Set.of(APPLIED);
            case APPLIED -> Set.of(INTERVIEW_SCHEDULED, REJECTED);
            case INTERVIEW_SCHEDULED -> Set.of(OFFERED, REJECTED);
            case OFFERED, REJECTED -> Set.of();
        };
    }

    public boolean canTransitionTo(ApplicationStatus targetStatus) {
        return targetStatus == this || allowedTransitions().contains(targetStatus);
    }
}
