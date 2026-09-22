package com.josyantl.joblens.document.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataApplicationDocumentRepository
        extends JpaRepository<ApplicationDocumentJpaEntity, Long> {
    List<ApplicationDocumentJpaEntity> findAllByApplicationIdAndUserIdOrderByCreatedAtDescIdDesc(
            Long applicationId, Long userId);
    Optional<ApplicationDocumentJpaEntity> findByIdAndUserId(Long id, Long userId);
}
