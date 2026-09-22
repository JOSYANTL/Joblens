package com.josyantl.joblens.activity.infrastructure.persistence;

import com.josyantl.joblens.activity.domain.model.ApplicationNote;
import com.josyantl.joblens.activity.domain.repository.ApplicationNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaApplicationNoteRepository implements ApplicationNoteRepository {
    private final SpringDataApplicationNoteRepository repository;

    @Override
    public ApplicationNote save(ApplicationNote note) {
        return toDomain(repository.saveAndFlush(toEntity(note)));
    }

    @Override
    public Optional<ApplicationNote> findById(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public List<ApplicationNote> findAll(Long userId, Long applicationId) {
        return repository.findByUserIdAndApplicationIdOrderByUpdatedAtDescIdDesc(userId, applicationId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(ApplicationNote note) {
        repository.delete(toEntity(note));
        repository.flush();
    }

    private ApplicationNoteJpaEntity toEntity(ApplicationNote note) {
        ApplicationNoteJpaEntity entity = new ApplicationNoteJpaEntity();
        entity.setId(note.getId());
        entity.setUserId(note.getUserId());
        entity.setApplicationId(note.getApplicationId());
        entity.setContent(note.getContent());
        entity.setCreatedAt(note.getCreatedAt());
        entity.setUpdatedAt(note.getUpdatedAt());
        entity.setVersion(note.getVersion());
        return entity;
    }

    private ApplicationNote toDomain(ApplicationNoteJpaEntity entity) {
        return ApplicationNote.restore(entity.getId(), entity.getUserId(), entity.getApplicationId(),
                entity.getContent(), entity.getCreatedAt(), entity.getUpdatedAt(), entity.getVersion());
    }
}
