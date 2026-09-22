package com.josyantl.joblens.activity.application;

import com.josyantl.joblens.activity.application.exception.ApplicationNoteNotFoundException;
import com.josyantl.joblens.activity.application.exception.StaleApplicationNoteVersionException;
import com.josyantl.joblens.activity.domain.model.ApplicationActivity;
import com.josyantl.joblens.activity.domain.model.ApplicationNote;
import com.josyantl.joblens.activity.domain.repository.ApplicationActivityPage;
import com.josyantl.joblens.activity.domain.repository.ApplicationActivityRepository;
import com.josyantl.joblens.activity.domain.repository.ApplicationNoteRepository;
import com.josyantl.joblens.job.application.exception.JobApplicationNotFoundException;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.shared.application.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationActivityService implements ApplicationActivityRecorder {
    private final ApplicationActivityRepository activities;
    private final ApplicationNoteRepository notes;
    private final JobApplicationRepository applications;
    private final CurrentUserProvider currentUser;

    public ApplicationActivityPage findActivities(Long applicationId, ApplicationActivityType type,
                                                   int page, int size) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        return activities.findAll(userId, applicationId, type, page, size);
    }

    public List<ApplicationNote> findNotes(Long applicationId) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        return notes.findAll(userId, applicationId);
    }

    @Transactional
    public ApplicationNote createNote(Long applicationId, String content) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        Instant now = Instant.now();
        ApplicationNote note = notes.save(ApplicationNote.create(userId, applicationId, content, now));
        record(userId, applicationId, ApplicationActivityType.NOTE_CREATED,
                ApplicationActivitySubjectType.NOTE, note.getId(), "添加了一条备注", now);
        return note;
    }

    @Transactional
    public ApplicationNote updateNote(Long applicationId, Long noteId, String content, long version) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        ApplicationNote note = getNote(applicationId, noteId, userId);
        requireVersion(note, version);
        Instant now = Instant.now();
        note.update(content, now);
        ApplicationNote saved = notes.save(note);
        record(userId, applicationId, ApplicationActivityType.NOTE_UPDATED,
                ApplicationActivitySubjectType.NOTE, saved.getId(), "更新了一条备注", now);
        return saved;
    }

    @Transactional
    public void deleteNote(Long applicationId, Long noteId, long version) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        ApplicationNote note = getNote(applicationId, noteId, userId);
        requireVersion(note, version);
        notes.delete(note);
        record(userId, applicationId, ApplicationActivityType.NOTE_DELETED,
                ApplicationActivitySubjectType.NOTE, noteId, "删除了一条备注", Instant.now());
    }

    @Override
    @Transactional
    public void record(Long userId, Long applicationId, ApplicationActivityType type,
            ApplicationActivitySubjectType subjectType, Long subjectId, String summary, Instant occurredAt) {
        activities.save(ApplicationActivity.create(userId, applicationId, type, subjectType,
                subjectId, summary, occurredAt));
    }

    private ApplicationNote getNote(Long applicationId, Long noteId, Long userId) {
        return notes.findById(noteId, userId)
                .filter(note -> note.getApplicationId().equals(applicationId))
                .orElseThrow(() -> new ApplicationNoteNotFoundException(noteId));
    }

    private void requireVersion(ApplicationNote note, long version) {
        if (note.getVersion() != version) throw new StaleApplicationNoteVersionException();
    }

    private void requireApplication(Long id, Long userId) {
        applications.findById(id, userId)
                .orElseThrow(() -> new JobApplicationNotFoundException(id));
    }
}
