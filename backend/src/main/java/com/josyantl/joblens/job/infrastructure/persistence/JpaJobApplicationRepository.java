package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
    public Optional<JobApplication> findById(Long id) {
        return springDataRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<JobApplication> findAll() {
        return springDataRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }
}
