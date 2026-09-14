package com.josyantl.joblens.job.domain.model;

import com.josyantl.joblens.job.domain.exception.InvalidApplicationStatusTransitionException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JobApplication {
    private Long id;
    private String company;
    private String position;
    private String description;
    private ApplicationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JobApplication create(
            String company,
            String position,
            String description
    ) {
        LocalDateTime now = LocalDateTime.now();
        return new JobApplication(
                null,
                requireText(company, "Company", 150),
                requireText(position, "Position", 150),
                requireText(description, "Description"),
                ApplicationStatus.SAVED,
                now,
                now
        );
    }

    public static JobApplication restore(
            Long id,
            String company,
            String position,
            String description,
            ApplicationStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Id must be positive");
        }

        return new JobApplication(
                id,
                requireText(company, "Company", 150),
                requireText(position, "Position", 150),
                requireText(description, "Description"),
                requireNonNull(status, "Status"),
                requireNonNull(createdAt, "Created at"),
                requireNonNull(updatedAt, "Updated at")
        );
    }

    public void changeStatus(ApplicationStatus newStatus) {
        ApplicationStatus validStatus = requireNonNull(newStatus, "Status");
        if (status == validStatus) {
            return;
        }
        if (!status.canTransitionTo(validStatus)) {
            throw new InvalidApplicationStatusTransitionException(status, validStatus);
        }

        status = validStatus;
        updatedAt = LocalDateTime.now();
    }

    public Set<ApplicationStatus> availableStatuses() {
        return status.allowedTransitions();
    }

    public void updateDetails(String company, String position, String description) {
        String validCompany = requireText(company, "Company", 150);
        String validPosition = requireText(position, "Position", 150);
        String validDescription = requireText(description, "Description");

        this.company = validCompany;
        this.position = validPosition;
        this.description = validDescription;
        updatedAt = LocalDateTime.now();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        String validValue = requireText(value, fieldName);
        if (validValue.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed " + maxLength + " characters"
            );
        }
        return validValue;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }
}
