package com.josyantl.joblens.activity.infrastructure.persistence;

import com.josyantl.joblens.shared.application.ApplicationActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataApplicationActivityRepository
        extends JpaRepository<ApplicationActivityJpaEntity, Long> {
    Page<ApplicationActivityJpaEntity> findByUserIdAndApplicationId(
            Long userId, Long applicationId, Pageable pageable);
    Page<ApplicationActivityJpaEntity> findByUserIdAndApplicationIdAndType(
            Long userId, Long applicationId, ApplicationActivityType type, Pageable pageable);
    List<ApplicationActivityJpaEntity> findByUserId(Long userId, Pageable pageable);
}
