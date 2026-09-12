package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.infrastructure.persistence.JobApplicationJpaEntity;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataJobApplicationRepository springDataRepository;

    @BeforeEach
    void clearDatabase() {
        springDataRepository.deleteAll();
    }

    @Test
    void createsJobApplication() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Example Company",
                                  "position": "Backend Engineer",
                                  "description": "Java Spring Boot PostgreSQL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.company").value("Example Company"))
                .andExpect(jsonPath("$.position").value("Backend Engineer"))
                .andExpect(jsonPath("$.status").value("SAVED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void rejectsBlankRequiredFields() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "",
                                  "position": "Backend Engineer",
                                  "description": "Java Spring Boot PostgreSQL"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsJobApplications() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Example Company",
                                  "position": "Backend Engineer",
                                  "description": "Java Spring Boot PostgreSQL"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company").value("Example Company"))
                .andExpect(jsonPath("$[0].position").value("Backend Engineer"))
                .andExpect(jsonPath("$[0].status").value("SAVED"));
    }

    @Test
    void updatesJobApplicationStatus() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(patch("/api/applications/{id}/status", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPLIED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(application.getId()))
                .andExpect(jsonPath("$.status").value("APPLIED"));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPLIED"));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingJobApplication() throws Exception {
        mockMvc.perform(patch("/api/applications/{id}/status", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPLIED"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Job application not found"))
                .andExpect(jsonPath("$.detail").value("Job application not found: 999999"));
    }

    @Test
    void rejectsInvalidStatus() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(patch("/api/applications/{id}/status", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findsJobApplicationById() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(get("/api/applications/{id}", application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(application.getId()))
                .andExpect(jsonPath("$.company").value("Example Company"))
                .andExpect(jsonPath("$.position").value("Backend Engineer"));
    }

    @Test
    void returnsNotFoundWhenGettingMissingJobApplication() throws Exception {
        mockMvc.perform(get("/api/applications/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Job application not found: 999999"));
    }

    @Test
    void updatesJobApplicationDetailsWithoutChangingStatus() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(put("/api/applications/{id}", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Updated Company",
                                  "position": "Senior Backend Engineer",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Updated Company"))
                .andExpect(jsonPath("$.position").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("SAVED"));

        mockMvc.perform(get("/api/applications/{id}", application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Updated Company"));
    }

    @Test
    void rejectsInvalidUpdatedDetails() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(put("/api/applications/{id}", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "",
                                  "position": "Backend Engineer",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletesJobApplication() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(delete("/api/applications/{id}", application.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/applications/{id}", application.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenDeletingMissingJobApplication() throws Exception {
        mockMvc.perform(delete("/api/applications/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    private JobApplicationJpaEntity saveJobApplication() {
        LocalDateTime now = LocalDateTime.now();
        JobApplicationJpaEntity application = new JobApplicationJpaEntity();
        application.setCompany("Example Company");
        application.setPosition("Backend Engineer");
        application.setDescription("Java Spring Boot PostgreSQL");
        application.setStatus(ApplicationStatus.SAVED);
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        return springDataRepository.save(application);
    }
}
