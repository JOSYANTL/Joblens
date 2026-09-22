package com.josyantl.joblens.activity.domain.repository;

import com.josyantl.joblens.activity.domain.model.ApplicationNote;
import java.util.List;
import java.util.Optional;

public interface ApplicationNoteRepository {
    ApplicationNote save(ApplicationNote note);
    Optional<ApplicationNote> findById(Long id, Long userId);
    List<ApplicationNote> findAll(Long userId, Long applicationId);
    void delete(ApplicationNote note);
}
