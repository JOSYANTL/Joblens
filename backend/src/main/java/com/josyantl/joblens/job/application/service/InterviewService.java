package com.josyantl.joblens.job.application.service;

import com.josyantl.joblens.job.application.exception.*;
import com.josyantl.joblens.job.domain.model.*;
import com.josyantl.joblens.job.domain.repository.*;
import lombok.RequiredArgsConstructor;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import com.josyantl.joblens.shared.application.ApplicationActivityRecorder;
import com.josyantl.joblens.shared.application.ApplicationActivitySubjectType;
import com.josyantl.joblens.shared.application.ApplicationActivityType;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {
    private final InterviewRepository repository;
    private final JobApplicationRepository applications;
    private final CurrentUserProvider currentUser;
    private final ApplicationActivityRecorder activityRecorder;

    @Transactional
    public Interview schedule(Long applicationId, InterviewDetails details) {
        requireApplication(applicationId);
        Interview saved = repository.save(Interview.schedule(applicationId, details, Instant.now()));
        activityRecorder.record(currentUser.userId(), applicationId, ApplicationActivityType.INTERVIEW_SCHEDULED,
                ApplicationActivitySubjectType.INTERVIEW, saved.getId(),
                "预约了第 " + saved.getDetails().round() + " 轮面试", Instant.now());
        return saved;
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
        Instant now = Instant.now();
        interview.reschedule(details, now);
        Interview saved = repository.save(interview);
        activityRecorder.record(currentUser.userId(), applicationId, ApplicationActivityType.INTERVIEW_RESCHEDULED,
                ApplicationActivitySubjectType.INTERVIEW, saved.getId(),
                "调整了第 " + saved.getDetails().round() + " 轮面试安排", now);
        return saved;
    }

    @Transactional
    public Interview changeStatus(Long applicationId, Long interviewId, InterviewStatus status, long version) {
        Interview interview = get(applicationId, interviewId);
        requireVersion(interview, version);
        InterviewStatus previous = interview.getStatus();
        Instant now = Instant.now();
        interview.changeStatus(status, now);
        Interview saved = repository.save(interview);
        if (previous != saved.getStatus())
            activityRecorder.record(currentUser.userId(), applicationId,
                    ApplicationActivityType.INTERVIEW_STATUS_CHANGED,
                    ApplicationActivitySubjectType.INTERVIEW, saved.getId(),
                    "面试状态从 " + previous + " 更新为 " + saved.getStatus(), now);
        return saved;
    }

    @Transactional
    public Interview recordFeedback(Long applicationId, Long interviewId, InterviewFeedback feedback, long version) {
        Interview interview = get(applicationId, interviewId);
        requireVersion(interview, version);
        Instant now = Instant.now();
        interview.recordFeedback(feedback, now);
        Interview saved = repository.save(interview);
        activityRecorder.record(currentUser.userId(), applicationId,
                ApplicationActivityType.INTERVIEW_FEEDBACK_UPDATED,
                ApplicationActivitySubjectType.INTERVIEW, saved.getId(),
                "更新了第 " + saved.getDetails().round() + " 轮面试反馈", now);
        return saved;
    }

    private void requireApplication(Long id) {
        applications.findById(id, currentUser.userId())
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }

    private void requireVersion(Interview interview, long version) {
        if (interview.getVersion() != version) throw new StaleInterviewVersionException();
    }
}
