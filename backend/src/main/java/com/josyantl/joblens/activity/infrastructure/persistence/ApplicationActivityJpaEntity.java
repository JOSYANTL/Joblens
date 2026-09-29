package com.josyantl.joblens.activity.infrastructure.persistence;

import com.josyantl.joblens.shared.application.ApplicationActivitySubjectType;
import com.josyantl.joblens.shared.application.ApplicationActivityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "application_activity_events")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationActivityJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 50)
    private ApplicationActivityType type;
    @Enumerated(EnumType.STRING) @Column(name = "subject_type", nullable = false, length = 30)
    private ApplicationActivitySubjectType subjectType;
    @Column(name = "subject_id")
    private Long subjectId;
    @Column(nullable = false, length = 500)
    private String summary;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
