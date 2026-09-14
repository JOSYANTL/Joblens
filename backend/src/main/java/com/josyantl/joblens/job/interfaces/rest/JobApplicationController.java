package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationStatusCommand;
import com.josyantl.joblens.job.application.service.JobApplicationService;
import com.josyantl.joblens.job.domain.model.ApplicationStatus;
import com.josyantl.joblens.job.domain.model.JobApplication;
import com.josyantl.joblens.job.domain.repository.JobApplicationSearchCriteria;
import com.josyantl.joblens.job.domain.repository.JobApplicationSortField;
import com.josyantl.joblens.job.domain.repository.SortDirection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Validated
public class JobApplicationController {

    private final JobApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplicationResponse create(
            @Valid @RequestBody CreateJobApplicationRequest request
    ) {
        CreateJobApplicationCommand command = new CreateJobApplicationCommand(
                request.company(),
                request.position(),
                request.description()
        );
        return JobApplicationResponse.from(service.create(command));
    }

    @GetMapping
    public JobApplicationPageResponse findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        JobApplicationSearchCriteria criteria = new JobApplicationSearchCriteria(
                keyword,
                status,
                page,
                size,
                JobApplicationSortField.fromApiValue(sortBy),
                SortDirection.fromApiValue(direction)
        );
        return JobApplicationPageResponse.from(service.search(criteria));
    }

    @GetMapping("/{id}")
    public JobApplicationResponse findById(@PathVariable Long id) {
        return JobApplicationResponse.from(service.findById(id));
    }

    @GetMapping("/{id}/available-statuses")
    public AvailableApplicationStatusesResponse findAvailableStatuses(
            @PathVariable Long id
    ) {
        JobApplication application = service.findById(id);
        return new AvailableApplicationStatusesResponse(
                application.getStatus(),
                application.availableStatuses()
        );
    }

    @GetMapping("/statistics")
    public JobApplicationStatisticsResponse getStatistics() {
        return JobApplicationStatisticsResponse.from(service.getStatistics());
    }

    @PutMapping("/{id}")
    public JobApplicationResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationRequest request
    ) {
        UpdateJobApplicationCommand command = new UpdateJobApplicationCommand(
                id,
                request.company(),
                request.position(),
                request.description(),
                request.version()
        );
        return JobApplicationResponse.from(service.update(command));
    }

    @PatchMapping("/{id}/status")
    public JobApplicationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationStatusRequest request
    ) {
        UpdateJobApplicationStatusCommand command =
                new UpdateJobApplicationStatusCommand(id, request.status(), request.version());
        return JobApplicationResponse.from(service.updateStatus(command));
    }

    @GetMapping("/{id}/status-history")
    public List<JobApplicationStatusHistoryResponse> findStatusHistory(
            @PathVariable Long id
    ) {
        return service.findStatusHistory(id)
                .stream()
                .map(JobApplicationStatusHistoryResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
