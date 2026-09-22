package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.exception.*;
import com.josyantl.joblens.job.domain.model.*;
import com.josyantl.joblens.job.domain.repository.*;
import lombok.RequiredArgsConstructor;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {
    private final InterviewRepository repository;
    private final JobApplicationRepository applications;
    private final CurrentUserProvider currentUser;

    @Transactional
    public Interview schedule(Long applicationId, InterviewDetails details) {
        requireApplication(applicationId);
        return repository.save(Interview.schedule(applicationId, details, Instant.now()));
    }

    public Interview get(Long applicationId, Long interviewId) {
        requireApplication(applicationId);
        return repository.findById(interviewId)
                .filter(interview -> interview.getApplicationId().equals(applicationId))
                .orElseThrow(() -> new InterviewNotFoundException(interviewId));
    }

    public InterviewPage search(InterviewQuery query) {
        if (query.applicationId() != null) requireApplication(query.applicationId());
        return repository.search(query, currentUser.userId());
    }

    @Transactional
    public Interview reschedule(Long applicationId, Long interviewId, InterviewDetails details, long version) {
        Interview interview = get(applicationId, interviewId);
        requireVersion(interview, version);
        interview.reschedule(details, Instant.now());
        return repository.save(interview);
    }

    @Transactional
    public Interview changeStatus(Long applicationId, Long interviewId, InterviewStatus status, long version) {
        Interview interview = get(applicationId, interviewId);
        requireVersion(interview, version);
        interview.changeStatus(status, Instant.now());
        return repository.save(interview);
    }

    @Transactional
    public Interview recordFeedback(Long applicationId, Long interviewId, InterviewFeedback feedback, long version) {
        Interview interview = get(applicationId, interviewId);
        requireVersion(interview, version);
        interview.recordFeedback(feedback, Instant.now());
        return repository.save(interview);
    }

    private void requireApplication(Long id) {
        applications.findById(id, currentUser.userId())
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }

    private void requireVersion(Interview interview, long version) {
        if (interview.getVersion() != version) throw new StaleInterviewVersionException();
    }
}
