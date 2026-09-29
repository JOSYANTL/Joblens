package com.josyantl.joblens.identity.interfaces.rest;

import com.josyantl.joblens.identity.infrastructure.persistence.SpringDataUserAccountRepository;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {
    private static final String PASSWORD = "secure-password-123";

    @Autowired MockMvc mvc;
    @Autowired SpringDataJobApplicationRepository applications;
    @Autowired SpringDataUserAccountRepository users;

    @BeforeEach
    void clearDatabase() {
        applications.deleteAll();
        users.deleteAll();
    }

    @Test
    void protectsApplicationEndpointsAndRequiresCsrfForRegistration() throws Exception {
        mvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("csrf@example.com", "CSRF User")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registersPersistsSessionAndStoresOnlyAnEncodedPassword() throws Exception {
        MvcResult registration = register("user@example.com", "Example User");
        HttpSession session = requiredSession(registration);

        mvc.perform(get("/api/auth/me").session(asMockSession(session)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.displayName").value("Example User"));

        String storedPassword = users.findByEmail("user@example.com").orElseThrow().getPasswordHash();
        assertThat(storedPassword).isNotEqualTo(PASSWORD).startsWith("{bcrypt}");
    }

    @Test
    void rejectsDuplicateRegistrationAndInvalidLogin() throws Exception {
        register("duplicate@example.com", "First User");

        mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("DUPLICATE@example.com", "Second User")))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("duplicate@example.com", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logsInAndLogsOutWithAHttpSession() throws Exception {
        register("login@example.com", "Login User");

        MvcResult login = mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("LOGIN@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        var session = asMockSession(requiredSession(login));

        mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void isolatesApplicationsInterviewsAndTasksBetweenUsers() throws Exception {
        var firstSession = asMockSession(requiredSession(register("first@example.com", "First User")));
        MvcResult created = mvc.perform(post("/api/applications").session(firstSession).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"company":"Private Company","position":"Engineer","description":"Private"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        Number applicationIdValue = com.jayway.jsonpath.JsonPath.read(
                created.getResponse().getContentAsString(), "$.id");
        long applicationId = applicationIdValue.longValue();

        mvc.perform(post("/api/applications/{id}/interviews", applicationId)
                        .session(firstSession).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"round":1,"type":"VIDEO","startsAt":"2030-01-01T00:00:00Z",
                                 "durationMinutes":60}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/applications/{id}/tasks", applicationId)
                        .session(firstSession).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Private task\",\"dueAt\":\"2030-01-02T00:00:00Z\"}"))
                .andExpect(status().isCreated());

        var secondSession = asMockSession(requiredSession(register("second@example.com", "Second User")));
        mvc.perform(get("/api/applications").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/applications/{id}", applicationId).session(secondSession))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/interviews").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(get("/api/tasks").session(secondSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(get("/api/applications").session(firstSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    private MvcResult register(String email, String displayName) throws Exception {
        return mvc.perform(post("/api/auth/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody(email, displayName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()))
                .andReturn();
    }

    private String registerBody(String email, String displayName) {
        return """
                {"email":"%s","password":"%s","displayName":"%s"}
                """.formatted(email, PASSWORD, displayName);
    }

    private String loginBody(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
    }

    private HttpSession requiredSession(MvcResult result) {
        return java.util.Objects.requireNonNull(result.getRequest().getSession(false));
    }

    private org.springframework.mock.web.MockHttpSession asMockSession(HttpSession session) {
        return (org.springframework.mock.web.MockHttpSession) session;
    }
}
