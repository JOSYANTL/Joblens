package com.josyantl.joblens.job.domain.model;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

class FollowUpTaskTest {
    private final Instant now = Instant.parse("2026-09-15T12:00:00Z");

    @Test
    void completionCancellationAndReopeningKeepTimestampsConsistent() {
        var task = FollowUpTask.create(1L, " Follow up ", null, now.minusSeconds(60), now);
        assertThat(task.getTitle()).isEqualTo("Follow up");
        assertThat(task.getNotes()).isEmpty();
        assertThat(task.isOverdue(now)).isTrue();
        task.changeStatus(FollowUpTaskStatus.DONE, now);
        assertThat(task.getCompletedAt()).isEqualTo(now);
        assertThat(task.isOverdue(now)).isFalse();
        task.changeStatus(FollowUpTaskStatus.TODO, now.plusSeconds(1));
        assertThat(task.getCompletedAt()).isNull();
        assertThat(task.isOverdue(now)).isTrue();
        task.changeStatus(FollowUpTaskStatus.CANCELLED, now.plusSeconds(2));
        assertThat(task.isOverdue(now)).isFalse();
    }

    @Test
    void repeatingStatusDoesNotChangeCompletionTime() {
        var task = FollowUpTask.create(1L, "Call", "", now, now);
        task.changeStatus(FollowUpTaskStatus.DONE, now);
        task.changeStatus(FollowUpTaskStatus.DONE, now.plusSeconds(60));
        assertThat(task.getCompletedAt()).isEqualTo(now);
        assertThat(task.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void deadlineBoundaryIsNotOverdue() {
        var task = FollowUpTask.create(1L, "Call", "", now, now);
        assertThat(task.isOverdue(now)).isFalse();
        assertThat(task.isOverdue(now.plusNanos(1))).isTrue();
    }

    @Test
    void invalidEditDoesNotPartiallyMutateTask() {
        var task = FollowUpTask.create(1L, "Original", "Notes", now, now);
        assertThatThrownBy(() -> task.update("Changed", "x".repeat(10001), now, now))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(task.getTitle()).isEqualTo("Original");
        assertThatThrownBy(() -> FollowUpTask.create(1L, " ", "", now, now))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> FollowUpTask.create(1L, "Valid", "", null, now))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
