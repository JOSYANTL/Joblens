package com.josyantl.joblens.document.infrastructure.persistence;

import com.josyantl.joblens.document.domain.model.ApplicationDocument;
import com.josyantl.joblens.document.domain.repository.ApplicationDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaApplicationDocumentRepository implements ApplicationDocumentRepository {
    private final SpringDataApplicationDocumentRepository repository;

    @Override
    public ApplicationDocument save(ApplicationDocument document) {
        return toDomain(repository.saveAndFlush(toEntity(document)));
    }

    @Override
    public List<ApplicationDocument> findAll(Long applicationId, Long userId) {
        return repository.findAllByApplicationIdAndUserIdOrderByCreatedAtDescIdDesc(applicationId, userId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<ApplicationDocument> findById(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public void delete(ApplicationDocument document) {
        repository.delete(toEntity(document));
        repository.flush();
    }

    private ApplicationDocumentJpaEntity toEntity(ApplicationDocument document) {
        ApplicationDocumentJpaEntity entity = new ApplicationDocumentJpaEntity();
        entity.setId(document.getId());
        entity.setUserId(document.getUserId());
        entity.setApplicationId(document.getApplicationId());
        entity.setType(document.getType());
        entity.setOriginalFileName(document.getOriginalFileName());
        entity.setContentType(document.getContentType());
        entity.setSizeBytes(document.getSizeBytes());
        entity.setStorageKey(document.getStorageKey());
        entity.setCreatedAt(document.getCreatedAt());
        return entity;
    }

    private ApplicationDocument toDomain(ApplicationDocumentJpaEntity entity) {
        return ApplicationDocument.restore(entity.getId(), entity.getUserId(), entity.getApplicationId(),
                entity.getType(), entity.getOriginalFileName(), entity.getContentType(),
                entity.getSizeBytes(), entity.getStorageKey(), entity.getCreatedAt());
    }
}
