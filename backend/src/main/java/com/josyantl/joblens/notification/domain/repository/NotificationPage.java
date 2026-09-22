package com.josyantl.joblens.notification.domain.repository;

import com.josyantl.joblens.notification.domain.model.Notification;
import java.util.List;

public record NotificationPage(List<Notification> content, int page, int size,
                               long totalElements, int totalPages) {}

