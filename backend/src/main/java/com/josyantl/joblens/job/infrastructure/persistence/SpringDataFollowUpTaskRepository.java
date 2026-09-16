package com.josyantl.joblens.job.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SpringDataFollowUpTaskRepository extends
        JpaRepository<FollowUpTaskJpaEntity, Long>, JpaSpecificationExecutor<FollowUpTaskJpaEntity> {}
