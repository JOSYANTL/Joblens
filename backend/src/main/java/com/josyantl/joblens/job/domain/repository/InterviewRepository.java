package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.Interview;
import java.util.Optional;

public interface InterviewRepository {
    Interview save(Interview interview);
    Optional<Interview> findById(Long id);
    InterviewPage search(InterviewQuery query, Long userId);
}
