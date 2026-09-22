package com.josyantl.joblens.notification.domain.repository;

import com.josyantl.joblens.notification.domain.model.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Notification save(Notification notification);
    boolean existsByDeduplicationKey(String key);
    Optional<Notification> findById(Long id, Long userId);
    NotificationPage search(Long userId, boolean unreadOnly, int page, int size);
    long countUnread(Long userId);
    List<Notification> findUnread(Long userId);
    void delete(Notification notification);
}

