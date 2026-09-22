package com.josyantl.joblens.document.domain.repository;

import com.josyantl.joblens.document.domain.model.ApplicationDocument;
import java.util.List;
import java.util.Optional;

public interface ApplicationDocumentRepository {
    ApplicationDocument save(ApplicationDocument document);
    List<ApplicationDocument> findAll(Long applicationId, Long userId);
    Optional<ApplicationDocument> findById(Long id, Long userId);
    void delete(ApplicationDocument document);
}
