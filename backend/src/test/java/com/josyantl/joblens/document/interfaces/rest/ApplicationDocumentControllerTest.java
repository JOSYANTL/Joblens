package com.josyantl.joblens.document.interfaces.rest;

import com.josyantl.joblens.document.infrastructure.persistence.SpringDataApplicationDocumentRepository;
import com.josyantl.joblens.identity.application.service.UserRegistrationService;
import com.josyantl.joblens.identity.infrastructure.persistence.SpringDataUserAccountRepository;
import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.service.JobApplicationService;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "joblens.documents.storage-root=${java.io.tmpdir}/joblens-document-tests")
@AutoConfigureMockMvc
@WithMockUser(username = "documents@example.com")
class ApplicationDocumentControllerTest {
    private static final Path STORAGE = Path.of(System.getProperty("java.io.tmpdir"), "joblens-document-tests");
    private static final byte[] PDF = "%PDF-1.7\nJoblens test document".getBytes();

    @Autowired MockMvc mvc;
    @Autowired UserRegistrationService users;
    @Autowired JobApplicationService applications;
    @Autowired SpringDataApplicationDocumentRepository documentEntities;
    @Autowired SpringDataJobApplicationRepository applicationEntities;
    @Autowired SpringDataUserAccountRepository userEntities;

    private Long applicationId;

    @BeforeEach
    void prepare() throws Exception {
        documentEntities.deleteAll();
        applicationEntities.deleteAll();
        userEntities.deleteAll();
        if (Files.exists(STORAGE)) {
            try (var files = Files.list(STORAGE)) {
                for (Path file : files.toList()) Files.deleteIfExists(file);
            }
        }
        users.register("documents@example.com", "secure-password-123", "Documents");
        applicationId = applications.create(new CreateJobApplicationCommand(
                "Acme", "Backend Engineer", "Java")).getId();
    }

    @Test
    void uploadsListsDownloadsAndDeletesAPdf() throws Exception {
        Long documentId = upload("resume.pdf", PDF, "RESUME");

        mvc.perform(get(path()).with(user("documents@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originalFileName").value("resume.pdf"))
                .andExpect(jsonPath("$[0].type").value("RESUME"))
                .andExpect(jsonPath("$[0].contentType").value("application/pdf"));
        mvc.perform(get(path() + "/{id}/content", documentId).with(user("documents@example.com")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(PDF));
        mvc.perform(delete(path() + "/{id}", documentId)
                        .with(user("documents@example.com")).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get(path()).with(user("documents@example.com"))).andExpect(jsonPath("$").isEmpty());
        assertThat(storedFileCount()).isZero();
    }

    @Test
    void rejectsDisguisedFilesAndHidesDocumentsFromOtherUsers() throws Exception {
        mvc.perform(multipart(path())
                        .file(new MockMultipartFile("file", "fake.pdf", "application/pdf", "not a pdf".getBytes()))
                        .param("type", "OTHER").with(user("documents@example.com")).with(csrf()))
                .andExpect(status().isBadRequest());

        Long documentId = upload("resume.pdf", PDF, "RESUME");
        users.register("other-documents@example.com", "secure-password-123", "Other");
        mvc.perform(get(path() + "/{id}/content", documentId)
                        .with(user("other-documents@example.com")))
                .andExpect(status().isNotFound());
        mvc.perform(delete(path() + "/{id}", documentId)
                        .with(user("other-documents@example.com")).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletingApplicationAlsoRemovesStoredFiles() throws Exception {
        upload("resume.pdf", PDF, "RESUME");
        assertThat(storedFileCount()).isEqualTo(1);

        mvc.perform(delete("/api/applications/{id}", applicationId)
                        .with(user("documents@example.com")).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(documentEntities.count()).isZero();
        assertThat(storedFileCount()).isZero();
    }

    private Long upload(String name, byte[] content, String type) throws Exception {
        mvc.perform(multipart(path())
                        .file(new MockMultipartFile("file", name, "application/octet-stream", content))
                        .param("type", type).with(user("documents@example.com")).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFileName").value(name))
                .andReturn();
        return documentEntities.findAll().getFirst().getId();
    }

    private String path() {
        return "/api/applications/" + applicationId + "/documents";
    }

    private long storedFileCount() throws Exception {
        if (!Files.exists(STORAGE)) return 0;
        try (var files = Files.list(STORAGE)) {
            return files.count();
        }
    }
}
