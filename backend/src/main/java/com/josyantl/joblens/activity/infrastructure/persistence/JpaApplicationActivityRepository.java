package com.josyantl.joblens.activity.infrastructure.persistence;

import com.josyantl.joblens.activity.domain.model.ApplicationActivity;
import com.josyantl.joblens.activity.domain.repository.ApplicationActivityPage;
import com.josyantl.joblens.activity.domain.repository.ApplicationActivityRepository;
import com.josyantl.joblens.shared.application.ApplicationActivityType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaApplicationActivityRepository implements ApplicationActivityRepository {
    private final SpringDataApplicationActivityRepository repository;

    @Override
    public ApplicationActivity save(ApplicationActivity activity) {
        return toDomain(repository.save(toEntity(activity)));
    }

    @Override
    public ApplicationActivityPage findAll(Long userId, Long applicationId,
            ApplicationActivityType type, int page, int size) {
        var pageable = PageRequest.of(page, size,
                Sort.by("occurredAt").descending().and(Sort.by("id").descending()));
        var result = type == null
                ? repository.findByUserIdAndApplicationId(userId, applicationId, pageable)
                : repository.findByUserIdAndApplicationIdAndType(userId, applicationId, type, pageable);
        return new ApplicationActivityPage(result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private ApplicationActivityJpaEntity toEntity(ApplicationActivity activity) {
        ApplicationActivityJpaEntity entity = new ApplicationActivityJpaEntity();
        entity.setId(activity.getId());
        entity.setUserId(activity.getUserId());
        entity.setApplicationId(activity.getApplicationId());
        entity.setType(activity.getType());
        entity.setSubjectType(activity.getSubjectType());
        entity.setSubjectId(activity.getSubjectId());
        entity.setSummary(activity.getSummary());
        entity.setOccurredAt(activity.getOccurredAt());
        return entity;
    }

    private ApplicationActivity toDomain(ApplicationActivityJpaEntity entity) {
        return ApplicationActivity.restore(entity.getId(), entity.getUserId(), entity.getApplicationId(),
                entity.getType(), entity.getSubjectType(), entity.getSubjectId(), entity.getSummary(),
                entity.getOccurredAt());
    }
}
