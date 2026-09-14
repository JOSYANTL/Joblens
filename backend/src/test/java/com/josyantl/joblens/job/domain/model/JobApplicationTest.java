package com.josyantl.joblens.job.domain.model;

import com.josyantl.joblens.job.domain.exception.InvalidApplicationStatusTransitionException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobApplicationTest {

    @Test
    void createsSavedJobApplicationWithTimestamps() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        );

        assertNull(application.getId());
        assertEquals("Example Company", application.getCompany());
        assertEquals("Backend Engineer", application.getPosition());
        assertEquals("Java and Spring Boot", application.getDescription());
        assertEquals(ApplicationStatus.SAVED, application.getStatus());
        assertNotNull(application.getCreatedAt());
        assertNotNull(application.getUpdatedAt());
        assertEquals(application.getCreatedAt(), application.getUpdatedAt());
    }

    @Test
    void rejectsBlankRequiredFields() {
        assertThrows(
                IllegalArgumentException.class,
                () -> JobApplication.create(" ", "Backend Engineer", "Description")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> JobApplication.create("Example Company", " ", "Description")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> JobApplication.create("Example Company", "Backend Engineer", " ")
        );
    }

    @Test
    void restoresPersistedJobApplication() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 8, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 9, 8, 11, 0);

        JobApplication application = JobApplication.restore(
                42L,
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot",
                ApplicationStatus.APPLIED,
                createdAt,
                updatedAt
        );

        assertEquals(42L, application.getId());
        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals(createdAt, application.getCreatedAt());
        assertEquals(updatedAt, application.getUpdatedAt());
    }

    @Test
    void changesStatusAndUpdatesTimestamp() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 8, 10, 0);
        LocalDateTime previousUpdatedAt = LocalDateTime.of(2026, 9, 8, 11, 0);
        JobApplication application = JobApplication.restore(
                42L,
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot",
                ApplicationStatus.SAVED,
                createdAt,
                previousUpdatedAt
        );

        application.changeStatus(ApplicationStatus.APPLIED);

        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertTrue(application.getUpdatedAt().isAfter(previousUpdatedAt));
        assertEquals(createdAt, application.getCreatedAt());
    }

    @Test
    void rejectsNullStatus() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        );

        assertThrows(IllegalArgumentException.class, () -> application.changeStatus(null));
    }

    @Test
    void updatesDetailsAndTimestampWithoutChangingStatus() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 8, 10, 0);
        LocalDateTime previousUpdatedAt = LocalDateTime.of(2026, 9, 8, 11, 0);
        JobApplication application = JobApplication.restore(
                42L,
                "Old Company",
                "Old Position",
                "Old description",
                ApplicationStatus.APPLIED,
                createdAt,
                previousUpdatedAt
        );

        application.updateDetails(
                "New Company",
                "Senior Backend Engineer",
                "Updated description"
        );

        assertEquals("New Company", application.getCompany());
        assertEquals("Senior Backend Engineer", application.getPosition());
        assertEquals("Updated description", application.getDescription());
        assertEquals(ApplicationStatus.APPLIED, application.getStatus());
        assertEquals(createdAt, application.getCreatedAt());
        assertTrue(application.getUpdatedAt().isAfter(previousUpdatedAt));
    }

    @Test
    void rejectsInvalidUpdatedDetails() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> application.updateDetails("Updated Company", " ", "Description")
        );
        assertEquals("Example Company", application.getCompany());
        assertEquals("Backend Engineer", application.getPosition());
        assertEquals("Java and Spring Boot", application.getDescription());
    }

    @Test
    void exposesAllowedStatusTransitions() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        );

        assertEquals(
                java.util.Set.of(ApplicationStatus.APPLIED),
                application.availableStatuses()
        );

        application.changeStatus(ApplicationStatus.APPLIED);

        assertEquals(
                java.util.Set.of(
                        ApplicationStatus.INTERVIEW_SCHEDULED,
                        ApplicationStatus.REJECTED
                ),
                application.availableStatuses()
        );
    }

    @Test
    void rejectsInvalidStatusTransitionWithoutChangingApplication() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        );
        LocalDateTime previousUpdatedAt = application.getUpdatedAt();

        assertThrows(
                InvalidApplicationStatusTransitionException.class,
                () -> application.changeStatus(ApplicationStatus.OFFERED)
        );
        assertEquals(ApplicationStatus.SAVED, application.getStatus());
        assertEquals(previousUpdatedAt, application.getUpdatedAt());
    }

    @Test
    void terminalStatusesHaveNoAvailableTransitions() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        );
        application.changeStatus(ApplicationStatus.APPLIED);
        application.changeStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        application.changeStatus(ApplicationStatus.OFFERED);

        assertTrue(application.availableStatuses().isEmpty());
        assertThrows(
                InvalidApplicationStatusTransitionException.class,
                () -> application.changeStatus(ApplicationStatus.REJECTED)
        );
    }
}
