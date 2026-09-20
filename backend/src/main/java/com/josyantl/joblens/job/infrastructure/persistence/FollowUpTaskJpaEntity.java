package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.FollowUpTaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "follow_up_tasks")
@Getter
@Setter
public class FollowUpTaskJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String notes;
    @Column(name = "due_at", nullable = false)
    private Instant dueAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FollowUpTaskStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Version
    @Column(nullable = false)
    private Long version;
}
