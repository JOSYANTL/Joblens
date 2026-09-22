package com.josyantl.joblens.notification.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {
    boolean existsByDeduplicationKey(String key);
    Optional<NotificationJpaEntity> findByIdAndUserId(Long id, Long userId);
    Page<NotificationJpaEntity> findAllByUserId(Long userId, Pageable pageable);
    Page<NotificationJpaEntity> findAllByUserIdAndReadAtIsNull(Long userId, Pageable pageable);
    long countByUserIdAndReadAtIsNull(Long userId);
    List<NotificationJpaEntity> findAllByUserIdAndReadAtIsNull(Long userId);
}

