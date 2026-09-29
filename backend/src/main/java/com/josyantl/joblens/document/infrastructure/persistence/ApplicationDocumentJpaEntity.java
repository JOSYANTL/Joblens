package com.josyantl.joblens.document.infrastructure.persistence;

import com.josyantl.joblens.document.domain.model.ApplicationDocumentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "application_documents")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationDocumentJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "application_id", nullable = false)
    private Long applicationId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationDocumentType type;
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;
    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;
    @Column(name = "storage_key", nullable = false, unique = true, length = 100)
    private String storageKey;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
