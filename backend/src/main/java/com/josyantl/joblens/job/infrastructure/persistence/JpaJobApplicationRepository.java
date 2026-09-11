package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaJobApplicationRepository implements JobApplicationRepository {

    private final SpringDataJobApplicationRepository springDataRepository;
    private final JobApplicationPersistenceMapper mapper;

    @Override
    public JobApplication save(JobApplication application) {
        JobApplicationJpaEntity savedEntity = springDataRepository.save(
                mapper.toEntity(application)
        );
        return mapper.toDomain(savedEntity);
    }

    @Override
    public List<JobApplication> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
