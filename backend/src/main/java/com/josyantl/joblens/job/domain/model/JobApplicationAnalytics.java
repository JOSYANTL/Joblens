package com.josyantl.joblens.job.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public record JobApplicationAnalytics(
        List<WeeklyApplications> weeklyApplications,
        Conversion conversion
) {
    public static final int WEEKS = 12;

    public JobApplicationAnalytics {
        weeklyApplications = List.copyOf(weeklyApplications);
    }

    public static JobApplicationAnalytics from(
            LocalDate today,
            List<LocalDateTime> creationDates,
            long applied,
            long interviewed,
            long offered
    ) {
        LocalDate firstWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(WEEKS - 1);
        long[] counts = new long[WEEKS];
        for (LocalDateTime creationDate : creationDates) {
            LocalDate weekStart = creationDate.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            int index = (int) java.time.temporal.ChronoUnit.WEEKS.between(firstWeek, weekStart);
            if (index >= 0 && index < WEEKS) {
                counts[index]++;
            }
        }
        List<WeeklyApplications> weeks = new ArrayList<>(WEEKS);
        for (int index = 0; index < WEEKS; index++) {
            weeks.add(new WeeklyApplications(firstWeek.plusWeeks(index), counts[index]));
        }
        return new JobApplicationAnalytics(weeks, new Conversion(applied, interviewed, offered));
    }

    public record WeeklyApplications(LocalDate weekStart, long count) {}

    public record Conversion(long applied, long interviewed, long offered) {
        public Conversion {
            if (applied < 0 || interviewed < 0 || offered < 0
                    || interviewed > applied || offered > interviewed) {
                throw new IllegalArgumentException("Conversion stages must be cumulative");
            }
        }
    }
}
