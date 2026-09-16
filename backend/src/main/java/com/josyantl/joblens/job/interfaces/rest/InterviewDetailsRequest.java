package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.InterviewDetails;
import com.josyantl.joblens.job.domain.model.InterviewType;
import jakarta.validation.constraints.*;
import java.time.Instant;

public record InterviewDetailsRequest(
        @NotNull @Min(1) @Max(100) Integer round,
        @NotNull InterviewType type,
        @NotNull Instant startsAt,
        @NotNull @Min(1) @Max(480) Integer durationMinutes,
        @Size(max = 200) String contact,
        @Size(max = 2000) String meetingUrl,
        @Size(max = 500) String location) {
    public InterviewDetails toDomain() {
        return new InterviewDetails(round, type, startsAt, durationMinutes, contact, meetingUrl, location);
    }
}
