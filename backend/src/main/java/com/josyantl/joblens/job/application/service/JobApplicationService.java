package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationStatusCommand;
import com.josyantl.joblens.job.application.exception.JobApplicationNotFoundException;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobApplicationService {

    private final JobApplicationRepository repository;

    @Transactional
    public JobApplication create(CreateJobApplicationCommand command) {
        JobApplication application = JobApplication.create(
                command.company(),
                command.position(),
                command.description()
        );
        return repository.save(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public JobApplication findById(Long id) {
        return getById(id);
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

        application.changeStatus(command.status());
        return repository.save(application);
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
