package com.josyantl.joblens.document.application;

import com.josyantl.joblens.document.application.exception.ApplicationDocumentNotFoundException;
import com.josyantl.joblens.document.domain.model.ApplicationDocument;
import com.josyantl.joblens.document.domain.model.ApplicationDocumentType;
import com.josyantl.joblens.document.domain.repository.ApplicationDocumentRepository;
import com.josyantl.joblens.job.application.exception.JobApplicationNotFoundException;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.shared.application.ApplicationDataCleanup;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class ApplicationDocumentService implements ApplicationDataCleanup {
    private static final String PDF = "application/pdf";
    private static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

    private final ApplicationDocumentRepository documents;
    private final JobApplicationRepository applications;
    private final DocumentStorage storage;
    private final CurrentUserProvider currentUser;

    @Transactional
    public ApplicationDocument upload(Long applicationId, ApplicationDocumentType type,
            String originalFileName, byte[] content) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        String fileName = safeFileName(originalFileName);
        String contentType = validateContent(fileName, content);
        String storageKey = UUID.randomUUID().toString();
        storage.store(storageKey, content);
        try {
            return documents.save(ApplicationDocument.create(userId, applicationId, type,
                    fileName, contentType, content.length, storageKey, Instant.now()));
        } catch (RuntimeException exception) {
            storage.delete(storageKey);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationDocument> findAll(Long applicationId) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        return documents.findAll(applicationId, userId);
    }

    @Transactional(readOnly = true)
    public DownloadedDocument download(Long applicationId, Long documentId) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        ApplicationDocument document = getDocument(documentId, userId);
        requireParent(document, applicationId);
        return new DownloadedDocument(document, storage.load(document.getStorageKey()));
    }

    @Transactional
    public void delete(Long applicationId, Long documentId) {
        Long userId = currentUser.userId();
        requireApplication(applicationId, userId);
        ApplicationDocument document = getDocument(documentId, userId);
        requireParent(document, applicationId);
        documents.delete(document);
        storage.delete(document.getStorageKey());
    }

    @Override
    @Transactional
    public void beforeApplicationDeleted(Long applicationId, Long userId) {
        for (ApplicationDocument document : documents.findAll(applicationId, userId)) {
            documents.delete(document);
            storage.delete(document.getStorageKey());
        }
    }

    private ApplicationDocument getDocument(Long id, Long userId) {
        return documents.findById(id, userId)
                .orElseThrow(() -> new ApplicationDocumentNotFoundException(id));
    }

    private void requireApplication(Long applicationId, Long userId) {
        applications.findById(applicationId, userId)
                .orElseThrow(() -> new JobApplicationNotFoundException(applicationId));
    }

    private void requireParent(ApplicationDocument document, Long applicationId) {
        if (!document.getApplicationId().equals(applicationId))
            throw new ApplicationDocumentNotFoundException(document.getId());
    }

    private String safeFileName(String supplied) {
        if (supplied == null) throw new IllegalArgumentException("File name is required");
        String normalized = supplied.replace('\\', '/');
        String name = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
        if (name.isBlank() || name.length() > 255 || name.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("File name must contain 1 to 255 safe characters");
        return name;
    }

    private String validateContent(String fileName, byte[] content) {
        if (content == null || content.length == 0)
            throw new IllegalArgumentException("File must not be empty");
        if (content.length > ApplicationDocument.MAX_SIZE_BYTES)
            throw new IllegalArgumentException("File must not exceed 10 MB");
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf") && startsWith(content, "%PDF-".getBytes())) return PDF;
        if (lower.endsWith(".docx") && isDocx(content)) return DOCX;
        throw new IllegalArgumentException("Only valid PDF and DOCX files are supported");
    }

    private boolean startsWith(byte[] content, byte[] prefix) {
        if (content.length < prefix.length) return false;
        for (int index = 0; index < prefix.length; index++)
            if (content[index] != prefix[index]) return false;
        return true;
    }

    private boolean isDocx(byte[] content) {
        boolean contentTypes = false;
        boolean documentXml = false;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                contentTypes |= "[Content_Types].xml".equals(entry.getName());
                documentXml |= "word/document.xml".equals(entry.getName());
                if (contentTypes && documentXml) return true;
            }
            return false;
        } catch (IOException exception) {
            return false;
        }
    }
}
