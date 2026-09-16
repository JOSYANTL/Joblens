package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.Interview;
import com.josyantl.joblens.job.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaInterviewRepository implements InterviewRepository {
    private final SpringDataInterviewRepository repository;
    private final InterviewMapper mapper;

    @Override
    public Interview save(Interview interview) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(interview)));
    }

    @Override
    public Optional<Interview> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public InterviewPage search(InterviewQuery criteria) {
        Specification<InterviewJpaEntity> specification = (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (criteria.applicationId() != null)
                predicate = cb.and(predicate, cb.equal(root.get("applicationId"), criteria.applicationId()));
            if (criteria.status() != null)
                predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            if (criteria.from() != null)
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("startsAt"), criteria.from()));
            if (criteria.to() != null)
                predicate = cb.and(predicate, cb.lessThan(root.get("startsAt"), criteria.to()));
            return predicate;
        };
        var page = repository.findAll(specification, PageRequest.of(criteria.page(), criteria.size(),
                Sort.by("startsAt").ascending().and(Sort.by("id").ascending())));
        return new InterviewPage(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
