package com.josyantl.joblens.activity.interfaces.rest;

import com.josyantl.joblens.identity.infrastructure.persistence.SpringDataUserAccountRepository;
import com.josyantl.joblens.identity.infrastructure.persistence.UserAccountJpaEntity;
import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.service.FollowUpTaskService;
import com.josyantl.joblens.job.application.service.JobApplicationService;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "activity@example.com")
class ApplicationActivityControllerTest {
    @Autowired MockMvc mvc;
    @Autowired JobApplicationService applications;
    @Autowired FollowUpTaskService tasks;
    @Autowired SpringDataJobApplicationRepository applicationRepository;
    @Autowired SpringDataUserAccountRepository users;
    private Long applicationId;

    @BeforeEach
    void prepare() {
        applicationRepository.deleteAll();
        ensureUser("activity@example.com");
        ensureUser("other@example.com");
        applicationId = applications.create(new CreateJobApplicationCommand(
                "Acme", "Backend Engineer", "Java")).getId();
    }

    @Test
    void createsUpdatesAndDeletesVersionedNotes() throws Exception {
        String base = "/api/applications/" + applicationId + "/notes";
        MvcResult created = mvc.perform(post(base).with(user("activity@example.com")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Recruiter prefers email\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Recruiter prefers email"))
                .andExpect(jsonPath("$.version").value(0)).andReturn();
        Number noteIdValue = com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(), "$.id");
        long noteId = noteIdValue.longValue();

        mvc.perform(get(base).with(user("activity@example.com"))).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Recruiter prefers email"));

        mvc.perform(put(base + "/" + noteId).with(user("activity@example.com")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Recruiter prefers a morning call\",\"version\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(put(base + "/" + noteId).with(user("activity@example.com")).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Stale update\",\"version\":0}"))
                .andExpect(status().isConflict());
        mvc.perform(delete(base + "/" + noteId).with(user("activity@example.com")).with(csrf()).param("version", "1"))
                .andExpect(status().isNoContent());
        mvc.perform(get(base).with(user("activity@example.com"))).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void recordsEventsFiltersByTypeAndPaginates() throws Exception {
        tasks.create(applicationId, "Send thank-you email", "", Instant.parse("2030-01-01T00:00:00Z"));
        mvc.perform(post("/api/applications/{id}/notes", applicationId).with(user("activity@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Important\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/applications/{id}/activities", applicationId).with(user("activity@example.com")).param("size", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].type").value("NOTE_CREATED"));
        mvc.perform(get("/api/applications/{id}/activities", applicationId).with(user("activity@example.com"))
                        .param("type", "TASK_CREATED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].subjectType").value("TASK"));
    }

    @Test
    void returnsAUserScopedRecentActivityFeed() throws Exception {
        tasks.create(applicationId, "Send thank-you email", "", Instant.parse("2030-01-01T00:00:00Z"));
        mvc.perform(post("/api/applications/{id}/notes", applicationId)
                        .with(user("activity@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Important\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/applications").with(user("other@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Private Company",
                                  "position": "Private Role",
                                  "description": "Must not appear in another user's feed"
                                }
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/activities/recent")
                        .with(user("activity@example.com"))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].applicationId").value(applicationId))
                .andExpect(jsonPath("$[0].type").value("NOTE_CREATED"))
                .andExpect(jsonPath("$[1].applicationId").value(applicationId))
                .andExpect(jsonPath("$[1].type").value("TASK_CREATED"));

        mvc.perform(get("/api/activities/recent")
                        .with(user("activity@example.com"))
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validatesInputAndKeepsOtherUsersDataPrivate() throws Exception {
        mvc.perform(post("/api/applications/{id}/notes", applicationId).with(user("activity@example.com")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"content\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/applications/{id}/activities", applicationId).with(user("activity@example.com")).param("size", "101"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/applications/{id}/activities", applicationId)
                        .with(user("other@example.com")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/applications/{id}/notes", applicationId)
                        .with(user("other@example.com")))
                .andExpect(status().isNotFound());
    }

    private void ensureUser(String email) {
        if (users.existsByEmail(email)) return;
        UserAccountJpaEntity user = new UserAccountJpaEntity();
        user.setEmail(email);
        user.setPasswordHash("{noop}password");
        user.setDisplayName(email);
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());
        users.saveAndFlush(user);
    }
}
