package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.identity.infrastructure.persistence.SpringDataUserAccountRepository;
import com.josyantl.joblens.identity.infrastructure.persistence.UserAccountJpaEntity;
import com.josyantl.joblens.job.infrastructure.persistence.JobApplicationJpaEntity;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com")
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataJobApplicationRepository springDataRepository;

    @Autowired
    private SpringDataUserAccountRepository userRepository;

    private Long userId;

    @BeforeEach
    void clearDatabase() {
        springDataRepository.deleteAll();
        userId = ensureTestUser().getId();
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
                .andExpect(jsonPath("$.version").value(0))
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
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].company").value("Example Company"))
                .andExpect(jsonPath("$.content[0].position").value("Backend Engineer"))
                .andExpect(jsonPath("$.content[0].status").value("SAVED"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void updatesJobApplicationStatus() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(patch("/api/applications/{id}/status", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPLIED",
                                  "version": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(application.getId()))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("APPLIED"));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingJobApplication() throws Exception {
        mockMvc.perform(patch("/api/applications/{id}/status", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APPLIED",
                                  "version": 0
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
                                  "status": "UNKNOWN",
                                  "version": 0
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
                                  "description": "Updated description",
                                  "version": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Updated Company"))
                .andExpect(jsonPath("$.position").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.status").value("SAVED"))
                .andExpect(jsonPath("$.version").value(1));

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
                                  "description": "Updated description",
                                  "version": 0
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

    @Test
    void filtersSearchesSortsAndPaginatesJobApplications() throws Exception {
        saveJobApplication("Beta Labs", "Backend Engineer", ApplicationStatus.SAVED);
        saveJobApplication("Alpha Systems", "Java Backend Developer", ApplicationStatus.SAVED);
        saveJobApplication("Gamma Studio", "Designer", ApplicationStatus.APPLIED);

        mockMvc.perform(get("/api/applications")
                        .param("status", "SAVED")
                        .param("keyword", "backend")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sortBy", "company")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].company").value("Alpha Systems"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void rejectsInvalidPaginationAndSorting() throws Exception {
        mockMvc.perform(get("/api/applications").param("size", "101"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/applications").param("sortBy", "id"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void recordsAndReturnsStatusHistory() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "History Company",
                                  "position": "Backend Engineer",
                                  "description": "History verification"
                                }
                                """))
                .andExpect(status().isCreated());
        Long id = springDataRepository.findAll().getFirst().getId();

        mockMvc.perform(patch("/api/applications/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\",\"version\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/applications/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\",\"version\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/applications/{id}/status-history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fromStatus").doesNotExist())
                .andExpect(jsonPath("$[0].toStatus").value("SAVED"))
                .andExpect(jsonPath("$[1].fromStatus").value("SAVED"))
                .andExpect(jsonPath("$[1].toStatus").value("APPLIED"));
    }

    @Test
    void returnsAvailableStatusTransitions() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(get("/api/applications/{id}/available-statuses", application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value("SAVED"))
                .andExpect(jsonPath("$.availableStatuses.length()").value(1))
                .andExpect(jsonPath("$.availableStatuses[0]").value("APPLIED"));
    }

    @Test
    void returnsConflictForInvalidStatusTransition() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(patch("/api/applications/{id}/status", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"OFFERED\",\"version\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title")
                        .value("Invalid application status transition"))
                .andExpect(jsonPath("$.detail")
                        .value("Cannot change application status from SAVED to OFFERED"));

        mockMvc.perform(get("/api/applications/{id}", application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SAVED"));
    }

    @Test
    void returnsDashboardStatisticsIncludingZeroCounts() throws Exception {
        saveJobApplication("Company A", "Engineer", ApplicationStatus.SAVED);
        saveJobApplication("Company B", "Developer", ApplicationStatus.SAVED);
        saveJobApplication("Company C", "Engineer", ApplicationStatus.APPLIED);

        mockMvc.perform(get("/api/applications/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.byStatus.SAVED").value(2))
                .andExpect(jsonPath("$.byStatus.APPLIED").value(1))
                .andExpect(jsonPath("$.byStatus.INTERVIEW_SCHEDULED").value(0))
                .andExpect(jsonPath("$.byStatus.OFFERED").value(0))
                .andExpect(jsonPath("$.byStatus.REJECTED").value(0));
    }

    @Test
    void rejectsStaleVersionWithoutOverwritingCurrentData() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(put("/api/applications/{id}", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "First Update",
                                  "position": "Backend Engineer",
                                  "description": "Current data",
                                  "version": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        mockMvc.perform(put("/api/applications/{id}", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Stale Update",
                                  "position": "Backend Engineer",
                                  "description": "Must not be saved",
                                  "version": 0
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Stale job application version"));

        mockMvc.perform(get("/api/applications/{id}", application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("First Update"))
                .andExpect(jsonPath("$.version").value(1));
    }

    @Test
    void returnsFieldErrorsWhenVersionIsMissing() throws Exception {
        JobApplicationJpaEntity application = saveJobApplication();

        mockMvc.perform(put("/api/applications/{id}", application.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Updated Company",
                                  "position": "Backend Engineer",
                                  "description": "Updated description"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.errors.version").value("must not be null"));
    }

    private JobApplicationJpaEntity saveJobApplication() {
        return saveJobApplication(
                "Example Company",
                "Backend Engineer",
                ApplicationStatus.SAVED
        );
    }

    private JobApplicationJpaEntity saveJobApplication(
            String company,
            String position,
            ApplicationStatus status
    ) {
        LocalDateTime now = LocalDateTime.now();
        JobApplicationJpaEntity application = new JobApplicationJpaEntity();
        application.setCompany(company);
        application.setPosition(position);
        application.setDescription("Java Spring Boot PostgreSQL");
        application.setStatus(status);
        application.setCreatedAt(now);
        application.setUpdatedAt(now);
        application.setUserId(userId);
        return springDataRepository.save(application);
    }

    private UserAccountJpaEntity ensureTestUser() {
        return userRepository.findByEmail("test@example.com").orElseGet(() -> {
            UserAccountJpaEntity user = new UserAccountJpaEntity();
            user.setEmail("test@example.com");
            user.setPasswordHash("{noop}test-password");
            user.setDisplayName("Test User");
            user.setEnabled(true);
            user.setCreatedAt(Instant.now());
            return userRepository.saveAndFlush(user);
        });
    }

    private static MockHttpServletRequestBuilder post(String path, Object... uriVariables) {
        return MockMvcRequestBuilders.post(path, uriVariables).with(user("test@example.com")).with(csrf());
    }

    private static MockHttpServletRequestBuilder put(String path, Object... uriVariables) {
        return MockMvcRequestBuilders.put(path, uriVariables).with(user("test@example.com")).with(csrf());
    }

    private static MockHttpServletRequestBuilder patch(String path, Object... uriVariables) {
        return MockMvcRequestBuilders.patch(path, uriVariables).with(user("test@example.com")).with(csrf());
    }

    private static MockHttpServletRequestBuilder delete(String path, Object... uriVariables) {
        return MockMvcRequestBuilders.delete(path, uriVariables).with(user("test@example.com")).with(csrf());
    }

    private static MockHttpServletRequestBuilder get(String path, Object... uriVariables) {
        return MockMvcRequestBuilders.get(path, uriVariables).with(user("test@example.com"));
    }
}
