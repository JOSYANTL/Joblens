package com.josyantl.joblens.job.infrastructure.persistence;

import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.repository.JobApplicationRepository;
import com.josyantl.joblens.job.domain.repository.JobApplicationPage;
import com.josyantl.joblens.job.domain.repository.JobApplicationSearchCriteria;
import com.josyantl.joblens.job.domain.repository.JobApplicationSortField;
import com.josyantl.joblens.job.domain.repository.SortDirection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({JpaJobApplicationRepository.class, JobApplicationPersistenceMapper.class})
class JpaJobApplicationRepositoryTest {
    private static final Long USER_ID = 1L;

    @Autowired
    private JobApplicationRepository repository;

    @Test
    void savesAndRestoresJobApplication() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java, Spring Boot, and PostgreSQL"
        );

        JobApplication savedApplication = repository.save(application, USER_ID);

        assertNotNull(savedApplication.getId());
        assertEquals(application.getCompany(), savedApplication.getCompany());
        assertEquals(application.getPosition(), savedApplication.getPosition());
        assertEquals(application.getDescription(), savedApplication.getDescription());
        assertEquals(ApplicationStatus.SAVED, savedApplication.getStatus());
        assertEquals(application.getCreatedAt(), savedApplication.getCreatedAt());
        assertEquals(application.getUpdatedAt(), savedApplication.getUpdatedAt());
    }

    @Test
    void findsAllJobApplications() {
        repository.save(JobApplication.create("Company A", "Engineer", "First role"), USER_ID);
        repository.save(JobApplication.create("Company B", "Developer", "Second role"), USER_ID);

        List<JobApplication> applications = repository.findAll(USER_ID);

        assertEquals(2, applications.size());
        assertEquals(
                Set.of("Company A", "Company B"),
                applications.stream().map(JobApplication::getCompany).collect(Collectors.toSet())
        );
    }

    @Test
    void findsJobApplicationById() {
        JobApplication savedApplication = repository.save(JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        ), USER_ID);

        JobApplication foundApplication = repository.findById(savedApplication.getId(), USER_ID)
                .orElseThrow();

        assertEquals(savedApplication.getId(), foundApplication.getId());
        assertEquals("Example Company", foundApplication.getCompany());
        assertEquals(ApplicationStatus.SAVED, foundApplication.getStatus());
    }

    @Test
    void deletesJobApplicationById() {
        JobApplication savedApplication = repository.save(JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java and Spring Boot"
        ), USER_ID);

        repository.deleteById(savedApplication.getId(), USER_ID);

        assertTrue(repository.findById(savedApplication.getId(), USER_ID).isEmpty());
    }

    @Test
    void searchesWithCombinedCriteriaAndPagination() {
        repository.save(JobApplication.create("Beta Labs", "Backend Engineer", "Java role"), USER_ID);
        repository.save(JobApplication.create("Alpha Systems", "Java Developer", "Backend role"), USER_ID);
        JobApplication rejected = JobApplication.create("Gamma", "Backend Engineer", "Java role");
        rejected.changeStatus(ApplicationStatus.APPLIED);
        rejected.changeStatus(ApplicationStatus.REJECTED);
        repository.save(rejected, USER_ID);

        JobApplicationPage result = repository.search(new JobApplicationSearchCriteria(
                "java",
                ApplicationStatus.SAVED,
                0,
                1,
                JobApplicationSortField.COMPANY,
                SortDirection.ASC
        ), USER_ID);

        assertEquals(1, result.content().size());
        assertEquals("Alpha Systems", result.content().getFirst().getCompany());
        assertEquals(2, result.totalElements());
        assertEquals(2, result.totalPages());
    }

    @Test
    void countsJobApplicationsGroupedByStatus() {
        repository.save(JobApplication.create("Company A", "Engineer", "First role"), USER_ID);
        repository.save(JobApplication.create("Company B", "Developer", "Second role"), USER_ID);
        JobApplication applied = JobApplication.create("Company C", "Engineer", "Third role");
        applied.changeStatus(ApplicationStatus.APPLIED);
        repository.save(applied, USER_ID);

        var counts = repository.countByStatus(USER_ID);

        assertEquals(2L, counts.get(ApplicationStatus.SAVED));
        assertEquals(1L, counts.get(ApplicationStatus.APPLIED));
    }
}
