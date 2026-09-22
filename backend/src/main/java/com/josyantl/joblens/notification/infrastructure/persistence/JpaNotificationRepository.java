package com.josyantl.joblens.notification.infrastructure.persistence;

import com.josyantl.joblens.notification.domain.model.Notification;
import com.josyantl.joblens.notification.domain.repository.NotificationPage;
import com.josyantl.joblens.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaNotificationRepository implements NotificationRepository {
    private final SpringDataNotificationRepository repository;

    @Override
    public Notification save(Notification notification) {
        return toDomain(repository.saveAndFlush(toEntity(notification)));
    }

    @Override
    public boolean existsByDeduplicationKey(String key) {
        return repository.existsByDeduplicationKey(key);
    }

    @Override
    public Optional<Notification> findById(Long id, Long userId) {
        return repository.findByIdAndUserId(id, userId).map(this::toDomain);
    }

    @Override
    public NotificationPage search(Long userId, boolean unreadOnly, int page, int size) {
        var pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        var result = unreadOnly
                ? repository.findAllByUserIdAndReadAtIsNull(userId, pageable)
                : repository.findAllByUserId(userId, pageable);
        return new NotificationPage(result.getContent().stream().map(this::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Override
    public long countUnread(Long userId) {
        return repository.countByUserIdAndReadAtIsNull(userId);
    }

    @Override
    public List<Notification> findUnread(Long userId) {
        return repository.findAllByUserIdAndReadAtIsNull(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public void delete(Notification notification) {
        repository.delete(toEntity(notification));
        repository.flush();
    }

    private NotificationJpaEntity toEntity(Notification notification) {
        NotificationJpaEntity entity = new NotificationJpaEntity();
        entity.setId(notification.getId());
        entity.setUserId(notification.getUserId());
        entity.setApplicationId(notification.getApplicationId());
        entity.setType(notification.getType());
        entity.setSourceType(notification.getSourceType());
        entity.setSourceId(notification.getSourceId());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setEventAt(notification.getEventAt());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setReadAt(notification.getReadAt());
        entity.setDeduplicationKey(notification.getDeduplicationKey());
        return entity;
    }

    private Notification toDomain(NotificationJpaEntity entity) {
        return Notification.restore(entity.getId(), entity.getUserId(), entity.getApplicationId(),
                entity.getType(), entity.getSourceType(), entity.getSourceId(), entity.getTitle(),
                entity.getMessage(), entity.getEventAt(), entity.getCreatedAt(), entity.getReadAt(),
                entity.getDeduplicationKey());
    }
}

