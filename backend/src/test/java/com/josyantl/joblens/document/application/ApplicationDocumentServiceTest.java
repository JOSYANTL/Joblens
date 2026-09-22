package com.josyantl.joblens.document.application;

import com.josyantl.joblens.document.domain.model.ApplicationDocument;
import com.josyantl.joblens.document.domain.repository.ApplicationDocumentRepository;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.shared.application.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentServiceTest {

    @Mock
    private ApplicationDocumentRepository documents;

    @Mock
    private JobApplicationRepository applications;

    @Mock
    private CurrentUserProvider currentUser;

    @Test
    void deletesStoredFilesAfterCommitWhenApplicationIsDeleted() {
        List<String> deletedKeys = new ArrayList<>();
        DocumentStorage storage = storage(deletedKeys);

        ApplicationDocument first = mock(ApplicationDocument.class);
        when(first.getStorageKey()).thenReturn("resume");
        ApplicationDocument second = mock(ApplicationDocument.class);
        when(second.getStorageKey()).thenReturn("cover-letter");
        when(documents.findAll(10L, 20L)).thenReturn(List.of(first, second));

        ApplicationDocumentService service = new ApplicationDocumentService(documents, applications, storage, currentUser);

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.beforeApplicationDeleted(10L, 20L);

            assertThat(deletedKeys).isEmpty();
            verify(documents).delete(first);
            verify(documents).delete(second);
            assertThat(TransactionSynchronizationManager.getSynchronizations()).hasSize(1);

            assertThatCode(() -> TransactionSynchronizationManager.getSynchronizations()
                    .forEach(TransactionSynchronization::afterCommit))
                    .doesNotThrowAnyException();

            assertThat(deletedKeys).containsExactly("resume", "cover-letter");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void deletesStoredFilesImmediatelyWhenSynchronizationIsUnavailable() {
        List<String> deletedKeys = new ArrayList<>();
        DocumentStorage storage = storage(deletedKeys);

        ApplicationDocument first = mock(ApplicationDocument.class);
        when(first.getStorageKey()).thenReturn("resume");
        ApplicationDocument second = mock(ApplicationDocument.class);
        when(second.getStorageKey()).thenReturn("cover-letter");
        when(documents.findAll(10L, 20L)).thenReturn(List.of(first, second));

        ApplicationDocumentService service = new ApplicationDocumentService(documents, applications, storage, currentUser);

        assertThatCode(() -> service.beforeApplicationDeleted(10L, 20L)).doesNotThrowAnyException();

        verify(documents).delete(first);
        verify(documents).delete(second);
        assertThat(deletedKeys).containsExactly("resume", "cover-letter");
    }

    private DocumentStorage storage(List<String> deletedKeys) {
        return new DocumentStorage() {
            @Override
            public void store(String key, byte[] content) {
                throw new UnsupportedOperationException();
            }

            @Override
            public byte[] load(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void delete(String key) {
                deletedKeys.add(key);
                if ("cover-letter".equals(key)) throw new RuntimeException("filesystem unavailable");
            }
        };
    }
}
