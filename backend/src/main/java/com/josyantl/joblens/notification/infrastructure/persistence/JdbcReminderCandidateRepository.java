package com.josyantl.joblens.notification.infrastructure.persistence;

import com.josyantl.joblens.notification.domain.repository.ReminderCandidate;
import com.josyantl.joblens.notification.domain.repository.ReminderCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcReminderCandidateRepository implements ReminderCandidateRepository {
    private final JdbcTemplate jdbc;

    @Override
    public List<ReminderCandidate> findUpcomingInterviews(Instant from, Instant to) {
        return jdbc.query("""
                SELECT a.user_id, i.application_id, i.id, a.company,
                       CONCAT('第 ', i.round_number, ' 轮面试'), i.starts_at
                FROM interviews i
                JOIN job_applications a ON a.id = i.application_id
                WHERE i.status = 'SCHEDULED' AND i.starts_at > ? AND i.starts_at <= ?
                """, this::map, Timestamp.from(from), Timestamp.from(to));
    }

    @Override
    public List<ReminderCandidate> findTasksDueSoon(Instant from, Instant to) {
        return jdbc.query("""
                SELECT a.user_id, t.application_id, t.id, a.company, t.title, t.due_at
                FROM follow_up_tasks t
                JOIN job_applications a ON a.id = t.application_id
                WHERE t.status = 'TODO' AND t.due_at > ? AND t.due_at <= ?
                """, this::map, Timestamp.from(from), Timestamp.from(to));
    }

    @Override
    public List<ReminderCandidate> findOverdueTasks(Instant now) {
        return jdbc.query("""
                SELECT a.user_id, t.application_id, t.id, a.company, t.title, t.due_at
                FROM follow_up_tasks t
                JOIN job_applications a ON a.id = t.application_id
                WHERE t.status = 'TODO' AND t.due_at < ?
                """, this::map, Timestamp.from(now));
    }

    private ReminderCandidate map(ResultSet result, int row) throws SQLException {
        return new ReminderCandidate(result.getLong(1), result.getLong(2), result.getLong(3),
                result.getString(4), result.getString(5), result.getTimestamp(6).toInstant());
    }
}
