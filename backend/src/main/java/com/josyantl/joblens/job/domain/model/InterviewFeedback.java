package com.josyantl.joblens.job.domain.model;

public record InterviewFeedback(String questions, String summary, String nextSteps) {
    public InterviewFeedback {
        questions = valid(questions);
        summary = valid(summary);
        nextSteps = valid(nextSteps);
    }

    public static InterviewFeedback empty() { return new InterviewFeedback("", "", ""); }

    private static String valid(String text) {
        if (text != null && text.length() > 10000)
            throw new IllegalArgumentException("Feedback fields must not exceed 10000 characters");
        return text == null ? "" : text;
    }
}
