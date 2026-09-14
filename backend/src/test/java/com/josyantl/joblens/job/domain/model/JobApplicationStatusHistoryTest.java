package com.josyantl.joblens.job.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobApplicationStatusHistoryTest {

    @Test
    void recordsCreationAndStatusChange() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 13, 16, 0);

        JobApplicationStatusHistory creation =
                JobApplicationStatusHistory.creation(1L, ApplicationStatus.SAVED, now);
        JobApplicationStatusHistory change = JobApplicationStatusHistory.change(
                1L,
                ApplicationStatus.SAVED,
                ApplicationStatus.APPLIED,
                now.plusMinutes(1)
        );

        assertNull(creation.fromStatus());
        assertEquals(ApplicationStatus.SAVED, creation.toStatus());
        assertEquals(ApplicationStatus.SAVED, change.fromStatus());
        assertEquals(ApplicationStatus.APPLIED, change.toStatus());
    }

    @Test
    void rejectsInvalidHistory() {
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                IllegalArgumentException.class,
                () -> JobApplicationStatusHistory.creation(null, ApplicationStatus.SAVED, now)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> JobApplicationStatusHistory.change(
                        1L,
                        null,
                        ApplicationStatus.APPLIED,
                        now
                )
        );
    }
}
