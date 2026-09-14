package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.repository.JobApplicationPage;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.job.domain.repository.JobApplicationSearchCriteria;
import com.josyantl.joblens.job.domain.repository.JobApplicationSortField;
import com.josyantl.joblens.job.domain.repository.SortDirection;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlJobApplicationIntegrationTest {

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

        assertThat(latestVersion).isEqualTo("4");
        assertThat(versionColumnCount).isEqualTo(1);
    }

    @Test
    void rejectsConcurrentUpdateAndKeepsFirstUpdate() {
        JobApplication saved = repository.save(JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Initial description"
        ));
        JobApplication firstCopy = repository.findById(saved.getId()).orElseThrow();
        JobApplication staleCopy = repository.findById(saved.getId()).orElseThrow();

        firstCopy.updateDetails(
                "First Writer",
                "Senior Backend Engineer",
                "First update"
        );
        JobApplication firstResult = repository.save(firstCopy);

        staleCopy.updateDetails(
                "Stale Writer",
                "Staff Backend Engineer",
                "Stale update"
        );

        assertThat(firstResult.getVersion()).isEqualTo(1L);
        assertThatThrownBy(() -> repository.save(staleCopy))
                .isInstanceOf(OptimisticLockingFailureException.class);

        JobApplication current = repository.findById(saved.getId()).orElseThrow();
        assertThat(current.getCompany()).isEqualTo("First Writer");
        assertThat(current.getVersion()).isEqualTo(1L);
    }

    @Test
    void searchesAndAggregatesUsingPostgres() {
        repository.save(JobApplication.create(
                "Acme",
                "Backend Engineer",
                "Java and PostgreSQL"
        ));
        JobApplication applied = repository.save(JobApplication.create(
                "Example Company",
                "Frontend Engineer",
                "React and TypeScript"
        ));
        applied.changeStatus(ApplicationStatus.APPLIED);
        repository.save(applied);

        JobApplicationPage result = repository.search(new JobApplicationSearchCriteria(
                "postgresql",
                ApplicationStatus.SAVED,
                0,
                10,
                JobApplicationSortField.CREATED_AT,
                SortDirection.DESC
        ));
        Map<ApplicationStatus, Long> counts = repository.countByStatus();

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).extracting(JobApplication::getCompany)
                .containsExactly("Acme");
        assertThat(counts.get(ApplicationStatus.SAVED)).isEqualTo(1L);
        assertThat(counts.get(ApplicationStatus.APPLIED)).isEqualTo(1L);
    }
}
