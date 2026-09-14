package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationStatusCommand;
import com.josyantl.joblens.job.application.exception.JobApplicationNotFoundException;
import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.model.JobApplicationStatusHistory;
import com.josyantl.joblens.job.domain.model.JobApplicationStatistics;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.job.domain.repository.JobApplicationPage;
import com.josyantl.joblens.job.domain.repository.JobApplicationSearchCriteria;
import com.josyantl.joblens.job.domain.repository.JobApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final JobApplicationStatusHistoryRepository statusHistoryRepository;

    @Transactional
    public JobApplication create(CreateJobApplicationCommand command) {
        JobApplication application = JobApplication.create(
                command.company(),
                command.position(),
                command.description()
        );
        JobApplication savedApplication = repository.save(application);
        statusHistoryRepository.save(JobApplicationStatusHistory.creation(
                savedApplication.getId(),
                savedApplication.getStatus(),
                savedApplication.getCreatedAt()
        ));
        return savedApplication;
    }

    @Transactional(readOnly = true)
    public List<JobApplication> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public JobApplicationPage search(JobApplicationSearchCriteria criteria) {
        return repository.search(criteria);
    }

    @Transactional(readOnly = true)
    public JobApplication findById(Long id) {
        return getById(id);
    }

    @Transactional(readOnly = true)
    public JobApplicationStatistics getStatistics() {
        return new JobApplicationStatistics(repository.countByStatus());
    }

    @Transactional
    public JobApplication update(UpdateJobApplicationCommand command) {
        JobApplication application = getById(command.id());
        application.updateDetails(
                command.company(),
                command.position(),
                command.description()
        );
        return repository.save(application);
    }

    @Transactional
    public JobApplication updateStatus(UpdateJobApplicationStatusCommand command) {
        JobApplication application = getById(command.id());
        ApplicationStatus previousStatus = application.getStatus();

        application.changeStatus(command.status());
        JobApplication savedApplication = repository.save(application);
        if (previousStatus != savedApplication.getStatus()) {
            statusHistoryRepository.save(JobApplicationStatusHistory.change(
                    savedApplication.getId(),
                    previousStatus,
                    savedApplication.getStatus(),
                    savedApplication.getUpdatedAt()
            ));
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
        repository.deleteById(id);
    }

    private JobApplication getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }
}
