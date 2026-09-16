package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.FollowUpTask;
import java.time.Instant;
import java.util.Optional;

public interface FollowUpTaskRepository {
    FollowUpTask save(FollowUpTask task);
    Optional<FollowUpTask> findById(Long id);
    FollowUpTaskPage search(FollowUpTaskQuery query, Instant now);
    void delete(FollowUpTask task);
}
