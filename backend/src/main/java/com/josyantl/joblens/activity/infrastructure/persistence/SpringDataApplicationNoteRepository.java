package com.josyantl.joblens.activity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataApplicationNoteRepository extends JpaRepository<ApplicationNoteJpaEntity, Long> {
    Optional<ApplicationNoteJpaEntity> findByIdAndUserId(Long id, Long userId);
    List<ApplicationNoteJpaEntity> findByUserIdAndApplicationIdOrderByUpdatedAtDescIdDesc(
            Long userId, Long applicationId);
}
