package com.josyantl.joblens.notification.domain.repository;

import java.time.Instant;

public record ReminderCandidate(Long userId, Long applicationId, Long sourceId,
                                String company, String label, Instant eventAt) {}

