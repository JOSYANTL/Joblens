package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
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

    @Transactional
    public JobApplication updateStatus(UpdateJobApplicationStatusCommand command) {
        JobApplication application = repository.findById(command.id())
                .orElseThrow(() -> new JobApplicationNotFoundException(command.id()));

        application.changeStatus(command.status());
        return repository.save(application);
    }
}
