package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.InterviewStatus;
import com.josyantl.joblens.job.domain.model.InterviewType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "interviews")
@Getter
@Setter
public class InterviewJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    @Column(name = "round_number", nullable = false)
    private int round;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private InterviewType type;
    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;
    @Column(nullable = false, length = 200)
    private String contact;
    @Column(name = "meeting_url", nullable = false, length = 2000)
    private String meetingUrl;
    @Column(nullable = false, length = 500)
    private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private InterviewStatus status;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questions;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;
    @Column(name = "next_steps", nullable = false, columnDefinition = "TEXT")
    private String nextSteps;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Version @Column(nullable = false)
    private Long version;
}
