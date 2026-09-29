package com.josyantl.joblens.job.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobApplicationAnalyticsTest {
    @Test
    void fillsEmptyWeeksAndCountsOnlyTheLastTwelveWeeks() {
        LocalDate today = LocalDate.of(2030, 1, 9);
        JobApplicationAnalytics analytics = JobApplicationAnalytics.from(today, List.of(
                LocalDateTime.of(2030, 1, 7, 10, 0),
                LocalDateTime.of(2030, 1, 9, 10, 0),
                LocalDateTime.of(2029, 10, 22, 10, 0),
                LocalDateTime.of(2029, 10, 15, 10, 0)
        ), 3, 2, 1);

        assertEquals(12, analytics.weeklyApplications().size());
        assertEquals(LocalDate.of(2029, 10, 22), analytics.weeklyApplications().getFirst().weekStart());
        assertEquals(1, analytics.weeklyApplications().getFirst().count());
        assertEquals(0, analytics.weeklyApplications().get(1).count());
        assertEquals(2, analytics.weeklyApplications().getLast().count());
    }

    @Test
    void rejectsNonCumulativeConversionCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> new JobApplicationAnalytics.Conversion(1, 2, 0));
    }
}
