package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.JobApplicationStatusHistory;
import com.josyantl.joblens.job.domain.repository.JobApplicationStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaJobApplicationStatusHistoryRepository
        implements JobApplicationStatusHistoryRepository {

    private final SpringDataJobApplicationStatusHistoryRepository springDataRepository;
    private final JobApplicationStatusHistoryPersistenceMapper mapper;

    @Override
    public JobApplicationStatusHistory save(JobApplicationStatusHistory history) {
        return mapper.toDomain(springDataRepository.save(mapper.toEntity(history)));
    }

    @Override
    public List<JobApplicationStatusHistory> findByJobApplicationId(Long jobApplicationId) {
        return springDataRepository
                .findByJobApplicationIdOrderByChangedAtAscIdAsc(jobApplicationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
