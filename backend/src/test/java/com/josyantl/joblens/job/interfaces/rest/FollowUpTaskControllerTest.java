package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.service.JobApplicationService;
import com.josyantl.joblens.job.application.service.FollowUpTaskService;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import com.josyantl.joblens.identity.infrastructure.persistence.SpringDataUserAccountRepository;
import com.josyantl.joblens.identity.infrastructure.persistence.UserAccountJpaEntity;
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
import java.time.Instant;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@example.com")
class FollowUpTaskControllerTest {
    @Autowired MockMvc mvc;
    @Autowired JobApplicationService applications;
    @Autowired FollowUpTaskService tasks;
    @Autowired SpringDataJobApplicationRepository repository;
    @Autowired SpringDataUserAccountRepository userRepository;
    private Long applicationId;

    @BeforeEach
    void prepare() {
        repository.deleteAll();
        ensureTestUser();
        applicationId = applications.create(new CreateJobApplicationCommand("Acme", "Engineer", "Java")).getId();
    }

    private void ensureTestUser() {
        if (userRepository.existsByEmail("test@example.com")) return;
        UserAccountJpaEntity user = new UserAccountJpaEntity();
        user.setEmail("test@example.com");
        user.setPasswordHash("{noop}test-password");
        user.setDisplayName("Test User");
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        userRepository.saveAndFlush(user);
    }

    private String path(Long taskId) {
        return "/api/applications/" + applicationId + "/tasks/" + taskId;
    }

    private Long task(String title, Instant dueAt) {
        return tasks.create(applicationId, title, "", dueAt).getId();
    }

    @Test
    void createsAndReadsTaskWithOffsetTimestamp() throws Exception {
        mvc.perform(post("/api/applications/" + applicationId + "/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Follow up","dueAt":"2026-09-15T20:00:00+08:00"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.dueAt").value("2026-09-15T12:00:00Z"))
                .andExpect(jsonPath("$.version").value(0));
        mvc.perform(get("/api/tasks").param("applicationId", applicationId.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void supportsEditingCompletionReopeningAndVersionedDeletion() throws Exception {
        Long id = task("Call", Instant.parse("2020-01-01T00:00:00Z"));
        mvc.perform(put(path(id)).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Email recruiter","notes":"Discuss next round",
                         "dueAt":"2030-01-01T00:00:00Z","version":0}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\",\"version\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andExpect(jsonPath("$.version").value(2));
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"TODO\",\"version\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completedAt").isEmpty())
                .andExpect(jsonPath("$.version").value(3));
        mvc.perform(delete(path(id)).param("version", "2")).andExpect(status().isConflict());
        mvc.perform(delete(path(id)).param("version", "3")).andExpect(status().isNoContent());
        mvc.perform(get(path(id))).andExpect(status().isNotFound());
    }

    @Test
    void staleStatusRequestDoesNotOverwriteCompletedTask() throws Exception {
        Long id = task("Call", Instant.now());
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DONE\",\"version\":0}")).andExpect(status().isOk());
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\",\"version\":0}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get(path(id))).andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void rejectsCrossApplicationAccessAndMissingParents() throws Exception {
        Long id = task("Call", Instant.now());
        Long other = applications.create(new CreateJobApplicationCommand("Other", "Engineer", "Java")).getId();
        mvc.perform(get("/api/applications/" + other + "/tasks/" + id)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/applications/" + other + "/tasks/" + id).param("version", "0"))
                .andExpect(status().isNotFound());
        mvc.perform(post("/api/applications/999999/tasks").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Call\",\"dueAt\":\"2030-01-01T00:00:00Z\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtersOverdueTasksAndPaginatesStably() throws Exception {
        Instant due = Instant.parse("2020-01-01T00:00:00Z");
        Long first = task("First", due);
        task("Second", due);
        Long done = task("Done", due);
        tasks.changeStatus(applicationId, done,
                com.josyantl.joblens.job.domain.model.FollowUpTaskStatus.DONE, 0);
        task("Future", Instant.parse("2100-01-01T00:00:00Z"));
        mvc.perform(get("/api/tasks").param("overdueOnly", "true").param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(first))
                .andExpect(jsonPath("$.content[0].overdue").value(true));
        mvc.perform(get("/api/tasks").param("dueFrom", "2020-01-01T00:00:00Z")
                .param("dueTo", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(delete("/api/applications/{id}", applicationId))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/tasks")).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void validatesRequestsAndQueryFilters() throws Exception {
        Long id = task("Call", Instant.now());
        mvc.perform(put(path(id)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Call\",\"dueAt\":\"2030-01-01T00:00:00Z\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.version").exists());
        mvc.perform(post("/api/applications/" + applicationId + "/tasks")
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.title").exists());
        mvc.perform(get("/api/tasks").param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("overdueOnly", "true").param("status", "DONE"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("dueFrom", "2030-01-01T00:00:00Z")
                .param("dueTo", "2020-01-01T00:00:00Z")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/tasks").param("dueFrom", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"UNKNOWN\",\"version\":0}")).andExpect(status().isBadRequest());
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
