package com.josyantl.joblens.notification.interfaces.rest;

import com.josyantl.joblens.identity.infrastructure.persistence.*;
import com.josyantl.joblens.identity.application.service.UserRegistrationService;
import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.service.*;
import com.josyantl.joblens.job.domain.model.*;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import com.josyantl.joblens.notification.application.service.NotificationReminderService;
import com.josyantl.joblens.notification.domain.model.*;
import com.josyantl.joblens.notification.domain.repository.NotificationRepository;
import com.josyantl.joblens.notification.infrastructure.persistence.SpringDataNotificationRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "notifications@example.com")
class NotificationControllerTest {
    private static final Instant NOW = Instant.parse("2030-01-01T00:00:00Z");

    @Autowired MockMvc mvc;
    @Autowired UserRegistrationService users;
    @Autowired SpringDataUserAccountRepository userEntities;
    @Autowired SpringDataNotificationRepository notificationEntities;
    @Autowired SpringDataJobApplicationRepository applicationEntities;
    @Autowired JobApplicationService applications;
    @Autowired InterviewService interviews;
    @Autowired FollowUpTaskService tasks;
    @Autowired NotificationReminderService reminders;
    @Autowired NotificationRepository notifications;

    private Long applicationId;
    private Long userId;

    @BeforeEach
    void prepare() {
        notificationEntities.deleteAll();
        applicationEntities.deleteAll();
        userEntities.deleteAll();
        userId = users.register("notifications@example.com", "secure-password-123", "Notifications").id();
        applicationId = applications.create(new CreateJobApplicationCommand("Acme", "Engineer", "Java")).getId();
        interviews.schedule(applicationId, new InterviewDetails(1, InterviewType.VIDEO,
                NOW.plusSeconds(12 * 3600), 60, "", "", ""));
        tasks.create(applicationId, "Due soon", "", NOW.plusSeconds(6 * 3600));
        tasks.create(applicationId, "Overdue", "", NOW.minusSeconds(3600));
    }

    @Test
    void generatesThreeKindsOfReminderWithoutDuplicates() throws Exception {
        assertThat(reminders.generate(NOW)).isEqualTo(3);
        assertThat(reminders.generate(NOW)).isZero();

        mvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[?(@.type == 'INTERVIEW_UPCOMING')]").isNotEmpty())
                .andExpect(jsonPath("$.content[?(@.type == 'TASK_DUE_SOON')]").isNotEmpty())
                .andExpect(jsonPath("$.content[?(@.type == 'TASK_OVERDUE')]").isNotEmpty());

        mvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void marksReadsDeletesAndHidesAnotherUsersNotifications() throws Exception {
        reminders.generate(NOW);
        Long ownId = notifications.search(userId, false, 0, 20).content().getFirst().getId();

        UserAccountJpaEntity other = new UserAccountJpaEntity();
        other.setEmail("other-notifications@example.com");
        other.setPasswordHash("{noop}disabled");
        other.setDisplayName("Other");
        other.setEnabled(true);
        other.setCreatedAt(NOW);
        Long otherId = userEntities.saveAndFlush(other).getId();
        var hidden = notifications.save(Notification.create(otherId, applicationId,
                NotificationType.TASK_OVERDUE, NotificationSourceType.TASK, 999L,
                "Hidden", "Hidden", NOW, "hidden:999", NOW));

        mvc.perform(patch("/api/notifications/{id}/read", ownId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.readAt").isNotEmpty());
        mvc.perform(get("/api/notifications/unread-count"))
                .andExpect(jsonPath("$.count").value(2));
        mvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications").param("unreadOnly", "true"))
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/notifications")).andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(patch("/api/notifications/{id}/read", hidden.getId()))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/notifications/{id}", ownId))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notifications")).andExpect(jsonPath("$.totalElements").value(2));
    }

    private static MockHttpServletRequestBuilder get(String path, Object... variables) {
        return MockMvcRequestBuilders.get(path, variables).with(user("notifications@example.com"));
    }

    private static MockHttpServletRequestBuilder patch(String path, Object... variables) {
        return MockMvcRequestBuilders.patch(path, variables)
                .with(user("notifications@example.com")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON);
    }

    private static MockHttpServletRequestBuilder delete(String path, Object... variables) {
        return MockMvcRequestBuilders.delete(path, variables)
                .with(user("notifications@example.com")).with(csrf());
    }
}
