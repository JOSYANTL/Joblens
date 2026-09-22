package com.josyantl.joblens.notification.domain.repository;

import java.time.Instant;
import java.util.List;

public interface ReminderCandidateRepository {
    List<ReminderCandidate> findUpcomingInterviews(Instant from, Instant to);
    List<ReminderCandidate> findTasksDueSoon(Instant from, Instant to);
    List<ReminderCandidate> findOverdueTasks(Instant now);
}

