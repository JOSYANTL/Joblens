package com.josyantl.joblens.job.domain.model;

import java.net.URI;
import java.time.Instant;

public record InterviewDetails(int round, InterviewType type, Instant startsAt,
        int durationMinutes, String contact, String meetingUrl, String location) {
    public InterviewDetails {
        if (round < 1 || round > 100) throw new IllegalArgumentException("Round must be between 1 and 100");
        if (type == null || startsAt == null) throw new IllegalArgumentException("Type and start time are required");
        if (durationMinutes < 1 || durationMinutes > 480)
            throw new IllegalArgumentException("Duration must be between 1 and 480 minutes");
        if (startsAt.isBefore(Instant.parse("1900-01-01T00:00:00Z"))
                || startsAt.isAfter(Instant.parse("9999-12-30T00:00:00Z")))
            throw new IllegalArgumentException("Start time is outside supported range");
        contact = text(contact, 200, "Contact");
        meetingUrl = text(meetingUrl, 2000, "Meeting URL");
        location = text(location, 500, "Location");
        if (!meetingUrl.isEmpty()) {
            URI uri;
            try { uri = URI.create(meetingUrl); }
            catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Meeting URL must be an absolute HTTP or HTTPS URL");
            }
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException("Meeting URL must be an absolute HTTP or HTTPS URL without credentials");
            }
        }
    }

    public Instant endsAt() { return startsAt.plusSeconds(durationMinutes * 60L); }

    private static String text(String value, int max, String field) {
        if (value == null) return "";
        if (value.length() > max) throw new IllegalArgumentException(field + " is too long");
        return value.trim();
    }
}
