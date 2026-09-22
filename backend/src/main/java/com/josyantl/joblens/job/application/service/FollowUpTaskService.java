package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.exception.*;
import com.josyantl.joblens.job.domain.model.*;
import com.josyantl.joblens.job.domain.repository.*;
import lombok.RequiredArgsConstructor;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import com.josyantl.joblens.shared.application.ApplicationActivityRecorder;
import com.josyantl.joblens.shared.application.ApplicationActivitySubjectType;
import com.josyantl.joblens.shared.application.ApplicationActivityType;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowUpTaskService {
    private final FollowUpTaskRepository repository;
    private final JobApplicationRepository applications;
    private final CurrentUserProvider currentUser;
    private final ApplicationActivityRecorder activityRecorder;

    @Transactional
    public FollowUpTask create(Long applicationId, String title, String notes, Instant dueAt) {
        requireApplication(applicationId);
        Instant now = Instant.now();
        FollowUpTask saved = repository.save(FollowUpTask.create(applicationId, title, notes, dueAt, now));
        activityRecorder.record(currentUser.userId(), applicationId, ApplicationActivityType.TASK_CREATED,
                ApplicationActivitySubjectType.TASK, saved.getId(), "创建了跟进任务：" + saved.getTitle(), now);
        return saved;
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
        Instant now = Instant.now();
        task.update(title, notes, dueAt, now);
        FollowUpTask saved = repository.save(task);
        activityRecorder.record(currentUser.userId(), applicationId, ApplicationActivityType.TASK_UPDATED,
                ApplicationActivitySubjectType.TASK, saved.getId(), "更新了跟进任务：" + saved.getTitle(), now);
        return saved;
    }

    @Transactional
    public FollowUpTask changeStatus(Long applicationId, Long taskId,
                                     FollowUpTaskStatus status, long version) {
        FollowUpTask task = get(applicationId, taskId);
        requireVersion(task, version);
        FollowUpTaskStatus previous = task.getStatus();
        Instant now = Instant.now();
        task.changeStatus(status, now);
        FollowUpTask saved = repository.save(task);
        if (previous != saved.getStatus())
            activityRecorder.record(currentUser.userId(), applicationId,
                    ApplicationActivityType.TASK_STATUS_CHANGED,
                    ApplicationActivitySubjectType.TASK, saved.getId(),
                    "任务状态从 " + previous + " 更新为 " + saved.getStatus(), now);
        return saved;
    }

    @Transactional
    public void delete(Long applicationId, Long taskId, long version) {
        FollowUpTask task = get(applicationId, taskId);
        requireVersion(task, version);
        repository.delete(task);
        activityRecorder.record(currentUser.userId(), applicationId, ApplicationActivityType.TASK_DELETED,
                ApplicationActivitySubjectType.TASK, taskId, "删除了跟进任务：" + task.getTitle(), Instant.now());
    }

    private void requireVersion(FollowUpTask task, long version) {
        if (task.getVersion() != version) throw new FollowUpTaskConflictException();
    }

    private void requireApplication(Long id) {
        applications.findById(id, currentUser.userId())
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }
}
