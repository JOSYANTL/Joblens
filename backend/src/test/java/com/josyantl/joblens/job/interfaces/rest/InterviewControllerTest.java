package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.service.InterviewService;
import com.josyantl.joblens.job.application.service.JobApplicationService;
import com.josyantl.joblens.job.domain.model.*;
import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InterviewControllerTest {
    @Autowired MockMvc mvc;
    @Autowired JobApplicationService applications;
    @Autowired InterviewService interviews;
    @Autowired SpringDataJobApplicationRepository repository;
    private Long applicationId;

    @BeforeEach
    void prepare() {
        repository.deleteAll();
        applicationId = applications.create(new CreateJobApplicationCommand("Acme", "Engineer", "Java")).getId();
    }

    private String path(Long id) { return "/api/applications/" + applicationId + "/interviews/" + id; }
    private Long schedule(Instant start) {
        return interviews.schedule(applicationId, new InterviewDetails(1, InterviewType.VIDEO,
                start, 60, "", "https://example.com/meeting", "")).getId();
    }

    @Test
    void createsInterviewWithUtcResponseAndDerivedEndTime() throws Exception {
        mvc.perform(post("/api/applications/" + applicationId + "/interviews")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"round":1,"type":"VIDEO","startsAt":"2030-01-01T18:00:00+08:00",
                         "durationMinutes":60,"contact":"Recruiter","meetingUrl":"https://example.com"}
                        """))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.startsAt").value("2030-01-01T10:00:00Z"))
                .andExpect(jsonPath("$.endsAt").value("2030-01-01T11:00:00Z"))
                .andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void reschedulesCompletesAndEditsFeedbackWithIndependentVersion() throws Exception {
        Long id = schedule(Instant.parse("2020-01-01T00:00:00Z"));
        mvc.perform(put(path(id)).contentType(MediaType.APPLICATION_JSON).content("""
                {"details":{"round":2,"type":"PHONE","startsAt":"2020-01-02T00:00:00Z",
                 "durationMinutes":30},"version":0}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.round").value(2));
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\",\"version\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.completedAt").isNotEmpty())
                .andExpect(jsonPath("$.version").value(2));
        mvc.perform(put(path(id) + "/feedback").contentType(MediaType.APPLICATION_JSON)
                .content("{\"questions\":\"Transactions?\",\"summary\":\"Good\",\"nextSteps\":\"Follow up\",\"version\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.feedback.summary").value("Good"))
                .andExpect(jsonPath("$.version").value(3));
        mvc.perform(put(path(id) + "/feedback").contentType(MediaType.APPLICATION_JSON)
                .content("{\"summary\":\"Revised\",\"version\":3}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(4));
        mvc.perform(get("/api/applications/" + applicationId))
                .andExpect(jsonPath("$.status").value("SAVED")).andExpect(jsonPath("$.version").value(0));
    }

    @Test
    void rejectsStaleFeedbackAndPreservesNewerValue() throws Exception {
        Long id = schedule(Instant.parse("2020-01-01T00:00:00Z"));
        interviews.changeStatus(applicationId, id, InterviewStatus.COMPLETED, 0);
        interviews.recordFeedback(applicationId, id, new InterviewFeedback("", "First", ""), 1);
        mvc.perform(put(path(id) + "/feedback").contentType(MediaType.APPLICATION_JSON)
                .content("{\"summary\":\"Stale\",\"version\":1}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get(path(id))).andExpect(jsonPath("$.feedback.summary").value("First"));
    }

    @Test
    void rejectsInvalidLifecycleAndKeepsCancelledRecord() throws Exception {
        Long id = schedule(Instant.parse("2100-01-01T00:00:00Z"));
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\",\"version\":0}")).andExpect(status().isConflict());
        mvc.perform(put(path(id) + "/feedback").contentType(MediaType.APPLICATION_JSON)
                .content("{\"summary\":\"Early\",\"version\":0}")).andExpect(status().isConflict());
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\",\"version\":0}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\",\"version\":1}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.version").value(1));
        mvc.perform(put(path(id)).contentType(MediaType.APPLICATION_JSON).content("""
                {"details":{"round":1,"type":"VIDEO","startsAt":"2100-01-02T00:00:00Z",
                 "durationMinutes":60},"version":1}
                """)).andExpect(status().isConflict());
        mvc.perform(get(path(id))).andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void rejectsCrossApplicationReadsAndWritesAndMissingParents() throws Exception {
        Long id = schedule(Instant.now());
        Long other = applications.create(new CreateJobApplicationCommand("Other", "Engineer", "Java")).getId();
        String otherPath = "/api/applications/" + other + "/interviews/" + id;
        mvc.perform(get(otherPath)).andExpect(status().isNotFound());
        mvc.perform(patch(otherPath + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\",\"version\":0}")).andExpect(status().isNotFound());
        mvc.perform(put(otherPath + "/feedback").contentType(MediaType.APPLICATION_JSON)
                .content("{\"summary\":\"Wrong parent\",\"version\":0}")).andExpect(status().isNotFound());
        mvc.perform(post("/api/applications/999999/interviews").contentType(MediaType.APPLICATION_JSON)
                .content("{\"round\":1,\"type\":\"PHONE\",\"startsAt\":\"2030-01-01T00:00:00Z\",\"durationMinutes\":30}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtersDateRangeAndPaginatesStablyThenCascadesDelete() throws Exception {
        Instant start = Instant.parse("2030-01-01T00:00:00Z");
        Long first = schedule(start);
        schedule(start);
        Long cancelled = schedule(start);
        interviews.changeStatus(applicationId, cancelled, InterviewStatus.CANCELLED, 0);
        schedule(start.plusSeconds(86400));
        mvc.perform(get("/api/interviews").param("applicationId", applicationId.toString())
                .param("status", "SCHEDULED").param("from", start.toString())
                .param("to", start.plusSeconds(86400).toString()).param("size", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(first)).andExpect(jsonPath("$.totalPages").value(2));
        applications.delete(applicationId);
        mvc.perform(get("/api/interviews")).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void upcomingIncludesOnlyScheduledInterviewsInNextSevenDays() throws Exception {
        Instant now = Instant.now();
        Long upcoming = schedule(now.plusSeconds(86400));
        Long cancelled = schedule(now.plusSeconds(172800));
        interviews.changeStatus(applicationId, cancelled, InterviewStatus.CANCELLED, 0);
        schedule(now.minusSeconds(86400));
        schedule(now.plusSeconds(864000));
        mvc.perform(get("/api/interviews/upcoming"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(upcoming));
    }

    @Test
    void validatesBodiesVersionsUrlsAndFilters() throws Exception {
        Long id = schedule(Instant.now());
        mvc.perform(post("/api/applications/" + applicationId + "/interviews")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.round").exists());
        mvc.perform(put(path(id)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"details\":{},\"version\":0}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors['details.startsAt']").exists());
        mvc.perform(patch(path(id) + "/status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.version").exists());
        mvc.perform(post("/api/applications/" + applicationId + "/interviews")
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"round":1,"type":"VIDEO","startsAt":"2030-01-01T00:00:00Z",
                         "durationMinutes":60,"meetingUrl":"javascript:alert(1)"}
                        """)).andExpect(status().isBadRequest());
        mvc.perform(get("/api/interviews").param("size", "101")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/interviews").param("from", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/interviews").param("from", "2030-01-02T00:00:00Z")
                .param("to", "2030-01-01T00:00:00Z")).andExpect(status().isBadRequest());
    }
}
