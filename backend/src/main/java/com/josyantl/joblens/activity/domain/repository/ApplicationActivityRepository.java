package com.josyantl.joblens.activity.domain.repository;

import com.josyantl.joblens.activity.domain.model.ApplicationActivity;
import com.josyantl.joblens.shared.application.ApplicationActivityType;

public interface ApplicationActivityRepository {
    ApplicationActivity save(ApplicationActivity activity);
    ApplicationActivityPage findAll(Long userId, Long applicationId, ApplicationActivityType type,
                                    int page, int size);
}
