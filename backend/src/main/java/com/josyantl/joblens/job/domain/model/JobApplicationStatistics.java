package com.josyantl.joblens.job.domain.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record JobApplicationStatistics(
        long total,
        Map<ApplicationStatus, Long> byStatus
) {
    public JobApplicationStatistics(Map<ApplicationStatus, Long> counts) {
        this(totalOf(counts), normalizedCounts(counts));
    }

    public JobApplicationStatistics {
        EnumMap<ApplicationStatus, Long> copy = new EnumMap<>(ApplicationStatus.class);
        copy.putAll(byStatus);
        if (total < 0 || copy.values().stream().mapToLong(Long::longValue).sum() != total) {
            throw new IllegalArgumentException("Statistics total must match status counts");
        }
        byStatus = Collections.unmodifiableMap(copy);
    }

    private static long totalOf(Map<ApplicationStatus, Long> counts) {
        return counts.values().stream().mapToLong(Long::longValue).sum();
    }

    private static Map<ApplicationStatus, Long> normalizedCounts(
            Map<ApplicationStatus, Long> counts
    ) {
        EnumMap<ApplicationStatus, Long> normalized =
                new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            normalized.put(status, counts.getOrDefault(status, 0L));
        }
        return normalized;
    }
}
