package com.josyantl.joblens.job.domain.model;

import com.josyantl.joblens.job.domain.exception.InterviewStateException;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

class InterviewTest {
    private final Instant now = Instant.parse("2030-01-01T12:00:00Z");
    private InterviewDetails details(Instant start) {
        return new InterviewDetails(1, InterviewType.VIDEO, start, 60, "Recruiter", "https://example.com/meeting", "");
    }

    @Test
    void completesAndRecordsFeedbackWithoutChangingCompletionTime() {
        var interview = Interview.schedule(1L, details(now.minusSeconds(3600)), now);
        interview.changeStatus(InterviewStatus.COMPLETED, now);
        interview.recordFeedback(new InterviewFeedback("JPA?", "Good discussion", "Follow up"), now.plusSeconds(1));
        interview.changeStatus(InterviewStatus.COMPLETED, now.plusSeconds(2));
        assertThat(interview.getCompletedAt()).isEqualTo(now);
        assertThat(interview.getUpdatedAt()).isEqualTo(now.plusSeconds(1));
        assertThat(interview.getFeedback().summary()).isEqualTo("Good discussion");
        assertThat(interview.getDetails().endsAt()).isEqualTo(now);
    }

    @Test
    void rejectsCompletionBeforeStartButAllowsExactStart() {
        var interview = Interview.schedule(1L, details(now.plusSeconds(1)), now);
        assertThatThrownBy(() -> interview.changeStatus(InterviewStatus.COMPLETED, now))
                .isInstanceOf(InterviewStateException.class);
        assertThat(interview.getStatus()).isEqualTo(InterviewStatus.SCHEDULED);
        interview.changeStatus(InterviewStatus.COMPLETED, now.plusSeconds(1));
        assertThat(interview.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
    }

    @Test
    void cancelledInterviewCannotBeRescheduledCompletedOrGivenFeedback() {
        var interview = Interview.schedule(1L, details(now), now);
        interview.changeStatus(InterviewStatus.CANCELLED, now);
        assertThatThrownBy(() -> interview.reschedule(details(now.plusSeconds(3600)), now))
                .isInstanceOf(InterviewStateException.class);
        assertThatThrownBy(() -> interview.changeStatus(InterviewStatus.COMPLETED, now))
                .isInstanceOf(InterviewStateException.class);
        assertThatThrownBy(() -> interview.recordFeedback(InterviewFeedback.empty(), now))
                .isInstanceOf(InterviewStateException.class);
        assertThat(interview.getCompletedAt()).isNull();
    }

    @Test
    void completedInterviewCannotBeReopenedOrRescheduled() {
        var interview = Interview.schedule(1L, details(now), now);
        interview.changeStatus(InterviewStatus.COMPLETED, now);
        assertThatThrownBy(() -> interview.changeStatus(InterviewStatus.SCHEDULED, now))
                .isInstanceOf(InterviewStateException.class);
        assertThatThrownBy(() -> interview.reschedule(details(now.plusSeconds(3600)), now))
                .isInstanceOf(InterviewStateException.class);
    }

    @Test
    void validatesScheduleAndFeedback() {
        assertThatThrownBy(() -> new InterviewDetails(0, InterviewType.VIDEO, now, 60, "", "", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InterviewDetails(1, InterviewType.VIDEO, now, 0, "", "", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InterviewDetails(1, InterviewType.VIDEO, now, 481, "", "", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InterviewDetails(1, InterviewType.VIDEO, now, 60, "", "javascript:alert(1)", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new InterviewFeedback("", "x".repeat(10001), ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reschedulingPreservesIdentityAndFeedbackRequiresCompletion() {
        var interview = Interview.schedule(1L, details(now), now);
        interview.reschedule(details(now.plusSeconds(3600)), now.plusSeconds(1));
        assertThat(interview.getCreatedAt()).isEqualTo(now);
        assertThat(interview.getDetails().startsAt()).isEqualTo(now.plusSeconds(3600));
        assertThatThrownBy(() -> interview.recordFeedback(new InterviewFeedback("", "Notes", ""), now))
                .isInstanceOf(InterviewStateException.class);
    }
}
