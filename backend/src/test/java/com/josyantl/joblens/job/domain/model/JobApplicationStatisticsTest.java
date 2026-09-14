package com.josyantl.joblens.job.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobApplicationStatisticsTest {

    @Test
    void calculatesTotalAndIncludesStatusesWithZeroCount() {
        JobApplicationStatistics statistics = new JobApplicationStatistics(Map.of(
                ApplicationStatus.SAVED, 2L,
                ApplicationStatus.APPLIED, 1L
        ));

        assertEquals(3L, statistics.total());
        assertEquals(2L, statistics.byStatus().get(ApplicationStatus.SAVED));
        assertEquals(0L, statistics.byStatus().get(ApplicationStatus.OFFERED));
        assertEquals(ApplicationStatus.values().length, statistics.byStatus().size());
    }
}
