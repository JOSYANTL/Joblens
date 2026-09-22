package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.FollowUpTask;
import com.josyantl.joblens.job.domain.model.FollowUpTaskStatus;
import com.josyantl.joblens.job.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.Subquery;
import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaFollowUpTaskRepository implements FollowUpTaskRepository {
    private final SpringDataFollowUpTaskRepository repository;
    private final FollowUpTaskMapper mapper;

    public FollowUpTask save(FollowUpTask task) {
        return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(task)));
    }

    public Optional<FollowUpTask> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    public FollowUpTaskPage search(FollowUpTaskQuery criteria, Instant now, Long userId) {
        Specification<FollowUpTaskJpaEntity> specification = (root, query, cb) -> {
            Subquery<Long> ownedApplications = query.subquery(Long.class);
            var application = ownedApplications.from(JobApplicationJpaEntity.class);
            ownedApplications.select(application.get("id"))
                    .where(cb.equal(application.get("userId"), userId));
            var predicate = root.get("applicationId").in(ownedApplications);
            if (criteria.applicationId() != null)
                predicate = cb.and(predicate, cb.equal(root.get("applicationId"), criteria.applicationId()));
            if (criteria.status() != null)
                predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            if (criteria.dueFrom() != null)
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("dueAt"), criteria.dueFrom()));
            if (criteria.dueTo() != null)
                predicate = cb.and(predicate, cb.lessThan(root.get("dueAt"), criteria.dueTo()));
            if (criteria.overdueOnly())
                predicate = cb.and(predicate, cb.equal(root.get("status"), FollowUpTaskStatus.TODO),
                        cb.lessThan(root.get("dueAt"), now));
            return predicate;
        };
        var page = repository.findAll(specification, PageRequest.of(criteria.page(), criteria.size(),
                Sort.by("dueAt").ascending().and(Sort.by("id").ascending())));
        return new FollowUpTaskPage(page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    public void delete(FollowUpTask task) {
        repository.delete(mapper.toEntity(task));
        repository.flush();
    }
}
