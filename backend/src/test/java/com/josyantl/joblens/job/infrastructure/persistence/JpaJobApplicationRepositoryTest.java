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

    @Autowired
    private JobApplicationRepository repository;

    @Test
    void savesAndRestoresJobApplication() {
        JobApplication application = JobApplication.create(
                "Example Company",
                "Backend Engineer",
                "Java, Spring Boot, and PostgreSQL"
        );

        JobApplication savedApplication = repository.save(application);

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
        repository.save(JobApplication.create("Company A", "Engineer", "First role"));
        repository.save(JobApplication.create("Company B", "Developer", "Second role"));

        List<JobApplication> applications = repository.findAll();

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
        ));

        JobApplication foundApplication = repository.findById(savedApplication.getId())
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
        ));

        repository.deleteById(savedApplication.getId());

        assertTrue(repository.findById(savedApplication.getId()).isEmpty());
    }

    @Test
    void searchesWithCombinedCriteriaAndPagination() {
        repository.save(JobApplication.create("Beta Labs", "Backend Engineer", "Java role"));
        repository.save(JobApplication.create("Alpha Systems", "Java Developer", "Backend role"));
        JobApplication rejected = JobApplication.create("Gamma", "Backend Engineer", "Java role");
        rejected.changeStatus(ApplicationStatus.REJECTED);
        repository.save(rejected);

        JobApplicationPage result = repository.search(new JobApplicationSearchCriteria(
                "java",
                ApplicationStatus.SAVED,
                0,
                1,
                JobApplicationSortField.COMPANY,
                SortDirection.ASC
        ));

        assertEquals(1, result.content().size());
        assertEquals("Alpha Systems", result.content().getFirst().getCompany());
        assertEquals(2, result.totalElements());
        assertEquals(2, result.totalPages());
    }
}
