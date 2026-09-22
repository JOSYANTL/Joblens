package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.exception.*;
import com.josyantl.joblens.job.domain.model.*;
import com.josyantl.joblens.job.domain.repository.*;
import lombok.RequiredArgsConstructor;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowUpTaskService {
    private final FollowUpTaskRepository repository;
    private final JobApplicationRepository applications;
    private final CurrentUserProvider currentUser;

    @Transactional
    public FollowUpTask create(Long applicationId, String title, String notes, Instant dueAt) {
        requireApplication(applicationId);
        return repository.save(FollowUpTask.create(applicationId, title, notes, dueAt, Instant.now()));
    }

    public FollowUpTask get(Long applicationId, Long taskId) {
        requireApplication(applicationId);
        return repository.findById(taskId).filter(task -> task.getApplicationId().equals(applicationId))
                .orElseThrow(() -> new FollowUpTaskNotFoundException(taskId));
    }

    public FollowUpTaskPage search(FollowUpTaskQuery query, Instant now) {
        if (query.applicationId() != null) requireApplication(query.applicationId());
        return repository.search(query, now, currentUser.userId());
    }

    @Transactional
    public FollowUpTask update(Long applicationId, Long taskId, String title, String notes,
                               Instant dueAt, long version) {
        FollowUpTask task = get(applicationId, taskId);
        requireVersion(task, version);
        task.update(title, notes, dueAt, Instant.now());
        return repository.save(task);
    }

    @Transactional
    public FollowUpTask changeStatus(Long applicationId, Long taskId,
                                     FollowUpTaskStatus status, long version) {
        FollowUpTask task = get(applicationId, taskId);
        requireVersion(task, version);
        task.changeStatus(status, Instant.now());
        return repository.save(task);
    }

    @Transactional
    public void delete(Long applicationId, Long taskId, long version) {
        FollowUpTask task = get(applicationId, taskId);
        requireVersion(task, version);
        repository.delete(task);
    }

    private void requireVersion(FollowUpTask task, long version) {
        if (task.getVersion() != version) throw new FollowUpTaskConflictException();
    }

    private void requireApplication(Long id) {
        applications.findById(id, currentUser.userId())
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }
}
