package com.josyantl.joblens.notification.infrastructure.persistence;

import com.josyantl.joblens.notification.domain.model.NotificationSourceType;
import com.josyantl.joblens.notification.domain.model.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class NotificationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 20)
    private NotificationSourceType sourceType;
    @Column(name = "source_id", nullable = false)
    private Long sourceId;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(name = "event_at", nullable = false)
    private Instant eventAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "read_at")
    private Instant readAt;
    @Column(name = "deduplication_key", nullable = false, unique = true, length = 200)
    private String deduplicationKey;
}

