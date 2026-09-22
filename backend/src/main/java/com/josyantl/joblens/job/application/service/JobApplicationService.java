package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationStatusCommand;
import com.josyantl.joblens.job.application.exception.JobApplicationNotFoundException;
import com.josyantl.joblens.job.application.exception.StaleJobApplicationVersionException;
import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.model.JobApplicationStatusHistory;
import com.josyantl.joblens.job.domain.model.JobApplicationStatistics;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.job.domain.repository.JobApplicationPage;
import com.josyantl.joblens.job.domain.repository.JobApplicationSearchCriteria;
import com.josyantl.joblens.job.domain.repository.JobApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.josyantl.joblens.shared.application.ApplicationDataCleanup;
import com.josyantl.joblens.shared.application.ApplicationActivityRecorder;
import com.josyantl.joblens.shared.application.ApplicationActivitySubjectType;
import com.josyantl.joblens.shared.application.ApplicationActivityType;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobApplicationStatusHistoryRepository statusHistoryRepository;
    private final CurrentUserProvider currentUser;
    private final List<ApplicationDataCleanup> cleanupHandlers;
    private final ApplicationActivityRecorder activityRecorder;

    @Transactional
    public JobApplication create(CreateJobApplicationCommand command) {
        JobApplication application = JobApplication.create(
                command.company(),
                command.position(),
                command.description()
        );
        Long userId = currentUser.userId();
        JobApplication savedApplication = repository.save(application, userId);
        statusHistoryRepository.save(JobApplicationStatusHistory.creation(
                savedApplication.getId(),
                savedApplication.getStatus(),
                savedApplication.getCreatedAt()
        ));
        activityRecorder.record(userId, savedApplication.getId(), ApplicationActivityType.APPLICATION_CREATED,
                ApplicationActivitySubjectType.APPLICATION, savedApplication.getId(),
                "创建了职位申请", Instant.now());
        return savedApplication;
    }

    @Transactional(readOnly = true)
    public List<JobApplication> findAll() {
        return repository.findAll(currentUser.userId());
    }

    @Transactional(readOnly = true)
    public JobApplicationPage search(JobApplicationSearchCriteria criteria) {
        return repository.search(criteria, currentUser.userId());
    }

    @Transactional(readOnly = true)
    public JobApplication findById(Long id) {
        return getById(id);
    }

    @Transactional(readOnly = true)
    public JobApplicationStatistics getStatistics() {
        return new JobApplicationStatistics(repository.countByStatus(currentUser.userId()));
    }

    @Transactional
    public JobApplication update(UpdateJobApplicationCommand command) {
        JobApplication application = getById(command.id());
        ensureCurrentVersion(application, command.version());
        application.updateDetails(
                command.company(),
                command.position(),
                command.description()
        );
        JobApplication saved = repository.save(application, currentUser.userId());
        activityRecorder.record(currentUser.userId(), saved.getId(), ApplicationActivityType.APPLICATION_UPDATED,
                ApplicationActivitySubjectType.APPLICATION, saved.getId(),
                "更新了申请信息", Instant.now());
        return saved;
    }

    @Transactional
    public JobApplication updateStatus(UpdateJobApplicationStatusCommand command) {
        JobApplication application = getById(command.id());
        ensureCurrentVersion(application, command.version());
        ApplicationStatus previousStatus = application.getStatus();

        application.changeStatus(command.status());
        JobApplication savedApplication = repository.save(application, currentUser.userId());
        if (previousStatus != savedApplication.getStatus()) {
            statusHistoryRepository.save(JobApplicationStatusHistory.change(
                    savedApplication.getId(),
                    previousStatus,
                    savedApplication.getStatus(),
                    savedApplication.getUpdatedAt()
            ));
            activityRecorder.record(currentUser.userId(), savedApplication.getId(),
                    ApplicationActivityType.APPLICATION_STATUS_CHANGED,
                    ApplicationActivitySubjectType.APPLICATION, savedApplication.getId(),
                    "申请状态从 " + previousStatus + " 更新为 " + savedApplication.getStatus(),
                    Instant.now());
        }
        return savedApplication;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationStatusHistory> findStatusHistory(Long id) {
        getById(id);
        return statusHistoryRepository.findByJobApplicationId(id);
    }

    @Transactional
    public void delete(Long id) {
        getById(id);
        Long userId = currentUser.userId();
        cleanupHandlers.forEach(handler -> handler.beforeApplicationDeleted(id, userId));
        repository.deleteById(id, userId);
    }

    private JobApplication getById(Long id) {
        return repository.findById(id, currentUser.userId())
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }

    private void ensureCurrentVersion(JobApplication application, Long expectedVersion) {
        if (expectedVersion == null || expectedVersion != application.getVersion()) {
            throw new StaleJobApplicationVersionException(
                    application.getId(),
                    expectedVersion == null ? -1L : expectedVersion,
                    application.getVersion()
            );
        }
    }
}
