package com.josyantl.joblens.notification.application.service;

import com.josyantl.joblens.notification.application.exception.NotificationNotFoundException;
import com.josyantl.joblens.notification.domain.model.Notification;
import com.josyantl.joblens.notification.domain.repository.NotificationPage;
import com.josyantl.joblens.notification.domain.repository.NotificationRepository;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository repository;
    private final CurrentUserProvider currentUser;

    public NotificationPage search(boolean unreadOnly, int page, int size) {
        if (page < 0 || size < 1 || size > 100)
            throw new IllegalArgumentException("Page must be nonnegative and size between 1 and 100");
        return repository.search(currentUser.userId(), unreadOnly, page, size);
    }

    public long countUnread() {
        return repository.countUnread(currentUser.userId());
    }

    @Transactional
    public Notification markRead(Long id) {
        Notification notification = get(id);
        notification.markRead(Instant.now());
        return repository.save(notification);
    }

    @Transactional
    public void markAllRead() {
        Instant now = Instant.now();
        repository.findUnread(currentUser.userId()).forEach(notification -> {
            notification.markRead(now);
            repository.save(notification);
        });
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(get(id));
    }

    private Notification get(Long id) {
        return repository.findById(id, currentUser.userId())
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}

