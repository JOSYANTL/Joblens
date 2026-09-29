package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.job.domain.repository.JobApplicationPage;
import com.josyantl.joblens.job.domain.repository.JobApplicationSearchCriteria;
import com.josyantl.joblens.job.domain.repository.SortDirection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaJobApplicationRepository implements JobApplicationRepository {

    private final SpringDataJobApplicationRepository springDataRepository;
    private final JobApplicationPersistenceMapper mapper;

    @Override
    public JobApplication save(JobApplication application, Long userId) {
        JobApplicationJpaEntity entity = mapper.toEntity(application);
        entity.setUserId(userId);
        JobApplicationJpaEntity savedEntity = springDataRepository.saveAndFlush(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<JobApplication> findById(Long id, Long userId) {
        return springDataRepository.findByIdAndUserId(id, userId)
                .map(mapper::toDomain);
    }

    @Override
    public List<JobApplication> findAll(Long userId) {
        return springDataRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public JobApplicationPage search(JobApplicationSearchCriteria criteria, Long userId) {
        Sort.Direction direction = criteria.direction() == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, criteria.sortBy().apiValue())
                .and(Sort.by(Sort.Direction.DESC, "id"));
        PageRequest pageable = PageRequest.of(
                criteria.page(),
                criteria.size(),
                sort
        );
        Page<JobApplicationJpaEntity> result = springDataRepository.findAll(
                buildSpecification(criteria, userId),
                pageable
        );

        return new JobApplicationPage(
                result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public Map<ApplicationStatus, Long> countByStatus(Long userId) {
        EnumMap<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        springDataRepository.countGroupedByStatus(userId)
                .forEach(row -> counts.put(row.getStatus(), row.getCount()));
        return counts;
    }

    private Specification<JobApplicationJpaEntity> buildSpecification(
            JobApplicationSearchCriteria criteria, Long userId
    ) {
        return (root, query, builder) -> {
            var predicate = builder.equal(root.get("userId"), userId);

            if (criteria.status() != null) {
                predicate = builder.and(
                        predicate,
                        builder.equal(root.get("status"), criteria.status())
                );
            }

            if (criteria.keyword() != null) {
                String pattern = "%" + criteria.keyword().toLowerCase(Locale.ROOT) + "%";
                predicate = builder.and(
                        predicate,
                        builder.or(
                                builder.like(builder.lower(root.get("company")), pattern),
                                builder.like(builder.lower(root.get("position")), pattern),
                                builder.like(builder.lower(root.get("description")), pattern)
                        )
                );
            }

            return predicate;
        };
    }

    @Override
    @Transactional
    public void deleteById(Long id, Long userId) {
        springDataRepository.deleteByIdAndUserId(id, userId);
    }
}
