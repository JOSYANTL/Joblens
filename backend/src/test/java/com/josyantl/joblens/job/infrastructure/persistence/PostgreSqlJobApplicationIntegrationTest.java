package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.model.Interview;
import com.josyantl.joblens.job.domain.model.InterviewDetails;
import com.josyantl.joblens.job.domain.model.InterviewFeedback;
import com.josyantl.joblens.job.domain.model.InterviewStatus;
import com.josyantl.joblens.job.domain.model.InterviewType;
import com.josyantl.joblens.job.domain.repository.InterviewRepository;
import com.josyantl.joblens.job.domain.repository.InterviewQuery;
import com.josyantl.joblens.job.domain.model.FollowUpTask;
import com.josyantl.joblens.job.domain.model.FollowUpTaskStatus;
import com.josyantl.joblens.job.domain.repository.FollowUpTaskRepository;
import com.josyantl.joblens.job.domain.repository.FollowUpTaskQuery;
import com.josyantl.joblens.job.domain.repository.JobApplicationPage;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.job.domain.repository.JobApplicationSearchCriteria;
import com.josyantl.joblens.job.domain.repository.JobApplicationSortField;
import com.josyantl.joblens.job.domain.repository.SortDirection;
import com.josyantl.joblens.notification.domain.repository.ReminderCandidateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlJobApplicationIntegrationTest {
    private static final Long USER_ID = 1L;

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16"));

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private JobApplicationRepository repository;

    @Autowired
    private SpringDataJobApplicationRepository springDataRepository;

    @Autowired
    private SpringDataJobApplicationStatusHistoryRepository historyRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private InterviewRepository interviews;

    @Autowired
    private ReminderCandidateRepository reminderCandidates;

    @Test
    void reminderCandidateQueriesBindInstantsInPostgres() {
        var application = repository.save(JobApplication.create("Acme", "Engineer", "Java"), USER_ID);
        var now = Instant.parse("2030-01-01T00:00:00Z");
        interviews.save(Interview.schedule(application.getId(),
                new InterviewDetails(2, InterviewType.VIDEO, now.plusSeconds(3600),
                        45, "", "", ""), now));
        tasks.save(FollowUpTask.create(application.getId(), "Prepare notes", "",
                now.plusSeconds(7200), now));

        assertThat(reminderCandidates.findUpcomingInterviews(now, now.plusSeconds(86400)))
                .singleElement().satisfies(candidate -> {
                    assertThat(candidate.userId()).isEqualTo(USER_ID);
                    assertThat(candidate.applicationId()).isEqualTo(application.getId());
        });
        assertThat(reminderCandidates.findTasksDueSoon(now, now.plusSeconds(86400)))
                .singleElement().satisfies(candidate -> assertThat(candidate.label()).isEqualTo("Prepare notes"));
        assertThat(reminderCandidates.findOverdueTasks(now)).isEmpty();
    }

    @Test
    void interviewsPersistFeedbackAndRejectConcurrentChangesInPostgres() {
        var application = repository.save(JobApplication.create("Acme", "Engineer", "Java"), USER_ID);
        var start = OffsetDateTime.parse("2020-01-01T18:00:00+08:00").toInstant();
        var details = new InterviewDetails(1, InterviewType.VIDEO, start, 60, "Recruiter",
                "https://example.com/meeting", "");
        var saved = interviews.save(Interview.schedule(application.getId(), details, Instant.now()));
        var first = interviews.findById(saved.getId()).orElseThrow();
        var stale = interviews.findById(saved.getId()).orElseThrow();
        assertThat(first.getDetails().startsAt()).isEqualTo(Instant.parse("2020-01-01T10:00:00Z"));
        first.changeStatus(InterviewStatus.COMPLETED, Instant.now());
        first.recordFeedback(new InterviewFeedback("JPA?", "Good", "Follow up"), Instant.now());
        assertThat(interviews.save(first).getVersion()).isEqualTo(1);
        stale.changeStatus(InterviewStatus.CANCELLED, Instant.now());
        assertThatThrownBy(() -> interviews.save(stale))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThat(interviews.findById(saved.getId()).orElseThrow().getFeedback().summary()).isEqualTo("Good");
        repository.deleteById(application.getId(), USER_ID);
        assertThat(interviews.findById(saved.getId())).isEmpty();
    }

    @Test
    void interviewCalendarFiltersAndPaginatesOnPostgres() {
        var application = repository.save(JobApplication.create("Acme", "Engineer", "Java"), USER_ID);
        var start = Instant.parse("2030-01-01T00:00:00Z");
        var details = new InterviewDetails(1, InterviewType.PHONE, start, 30, "", "", "");
        var first = interviews.save(Interview.schedule(application.getId(), details, Instant.now()));
        interviews.save(Interview.schedule(application.getId(), details, Instant.now()));
        var cancelled = Interview.schedule(application.getId(), details, Instant.now());
        cancelled.changeStatus(InterviewStatus.CANCELLED, Instant.now());
        interviews.save(cancelled);
        interviews.save(Interview.schedule(application.getId(),
                new InterviewDetails(2, InterviewType.PHONE, start.plusSeconds(86400), 30, "", "", ""), Instant.now()));
        var page = interviews.search(new InterviewQuery(application.getId(), InterviewStatus.SCHEDULED,
                start, start.plusSeconds(86400), 0, 1), USER_ID);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.content().getFirst().getId()).isEqualTo(first.getId());
    }

    @Autowired
    private FollowUpTaskRepository tasks;

    @Test
    void taskMigrationSupportsTimezonesOptimisticLockingAndCascadeDelete() {
        var application = repository.save(JobApplication.create("Acme", "Engineer", "Java"), USER_ID);
        var due = OffsetDateTime.parse("2030-01-01T18:00:00+08:00").toInstant();
        var saved = tasks.save(FollowUpTask.create(
                application.getId(), "Follow up", "", due, Instant.now()));
        var first = tasks.findById(saved.getId()).orElseThrow();
        var stale = tasks.findById(saved.getId()).orElseThrow();
        assertThat(first.getDueAt()).isEqualTo(Instant.parse("2030-01-01T10:00:00Z"));
        first.changeStatus(FollowUpTaskStatus.DONE, Instant.now());
        assertThat(tasks.save(first).getVersion()).isEqualTo(1);
        stale.update("Stale title", "", due, Instant.now());
        assertThatThrownBy(() -> tasks.save(stale))
                .isInstanceOf(OptimisticLockingFailureException.class);
        assertThatThrownBy(() -> tasks.delete(stale))
                .isInstanceOf(OptimisticLockingFailureException.class);
        repository.deleteById(application.getId(), USER_ID);
        assertThat(tasks.findById(saved.getId())).isEmpty();
    }

    @Test
    void taskOverdueSearchUsesPostgresAndExcludesCompletedTasks() {
        var application = repository.save(JobApplication.create("Acme", "Engineer", "Java"), USER_ID);
        var now = Instant.parse("2030-01-01T00:00:00Z");
        tasks.save(FollowUpTask.create(
                application.getId(), "Overdue", "", now.minusSeconds(1), now));
        var completed = FollowUpTask.create(
                application.getId(), "Completed", "", now.minusSeconds(1), now);
        completed.changeStatus(FollowUpTaskStatus.DONE, now);
        tasks.save(completed);
        tasks.save(FollowUpTask.create(
                application.getId(), "Boundary", "", now, now));
        var result = tasks.search(new FollowUpTaskQuery(
                application.getId(), null, null, null, true, 0, 20), now, USER_ID);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content().getFirst().getTitle()).isEqualTo("Overdue");
    }

    @BeforeEach
    void cleanDatabase() {
        historyRepository.deleteAll();
        springDataRepository.deleteAll();
    }

    @Test
    void appliesFlywayMigrationsToPostgres() {
        String latestVersion = jdbcTemplate.queryForObject(
                "SELECT version FROM flyway_schema_history "
                        + "WHERE success = TRUE ORDER BY installed_rank DESC LIMIT 1",
                String.class
        );
        Integer versionColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = 'public' "
                        + "AND table_name = 'job_applications' "
                        + "AND column_name = 'version'",
                Integer.class
        );
        Integer recentActivityIndexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes "
                        + "WHERE schemaname = 'public' "
                        + "AND tablename = 'application_activity_events' "
                        + "AND indexname = 'idx_application_activity_events_user_recent'",
                Integer.class
        );

        assertThat(latestVersion).isEqualTo("11");
        assertThat(versionColumnCount).isEqualTo(1);
        assertThat(recentActivityIndexCount).isEqualTo(1);
    }

    @Test
    void rejectsConcurrentUpdateAndKeepsFirstUpdate() {
        JobApplication saved = repository.save(JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Initial description"
        ), USER_ID);
        JobApplication firstCopy = repository.findById(saved.getId(), USER_ID).orElseThrow();
        JobApplication staleCopy = repository.findById(saved.getId(), USER_ID).orElseThrow();

        firstCopy.updateDetails(
                "First Writer",
                "Senior Backend Engineer",
                "First update"
        );
        JobApplication firstResult = repository.save(firstCopy, USER_ID);

        staleCopy.updateDetails(
                "Stale Writer",
                "Staff Backend Engineer",
                "Stale update"
        );

        assertThat(firstResult.getVersion()).isEqualTo(1L);
        assertThatThrownBy(() -> repository.save(staleCopy, USER_ID))
                .isInstanceOf(OptimisticLockingFailureException.class);

        JobApplication current = repository.findById(saved.getId(), USER_ID).orElseThrow();
        assertThat(current.getCompany()).isEqualTo("First Writer");
        assertThat(current.getVersion()).isEqualTo(1L);
    }

    @Test
    void searchesAndAggregatesUsingPostgres() {
        repository.save(JobApplication.create(
                "Acme",
                "Backend Engineer",
                "Java and PostgreSQL"
        ), USER_ID);
        JobApplication applied = repository.save(JobApplication.create(
                "Example Company",
                "Frontend Engineer",
                "React and TypeScript"
        ), USER_ID);
        applied.changeStatus(ApplicationStatus.APPLIED);
        repository.save(applied, USER_ID);

        JobApplicationPage result = repository.search(new JobApplicationSearchCriteria(
                "postgresql",
                ApplicationStatus.SAVED,
                0,
                10,
                JobApplicationSortField.CREATED_AT,
                SortDirection.DESC
        ), USER_ID);
        Map<ApplicationStatus, Long> counts = repository.countByStatus(USER_ID);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).extracting(JobApplication::getCompany)
                .containsExactly("Acme");
        assertThat(counts.get(ApplicationStatus.SAVED)).isEqualTo(1L);
        assertThat(counts.get(ApplicationStatus.APPLIED)).isEqualTo(1L);
    }
}
