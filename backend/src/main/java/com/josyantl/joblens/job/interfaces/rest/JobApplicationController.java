package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.application.command.CreateJobApplicationCommand;
import com.josyantl.joblens.job.application.command.UpdateJobApplicationStatusCommand;
import com.josyantl.joblens.job.application.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
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
    public List<JobApplicationResponse> findAll() {
        return service.findAll()
                .stream()
                .map(JobApplicationResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/status")
    public JobApplicationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationStatusRequest request
    ) {
        UpdateJobApplicationStatusCommand command =
                new UpdateJobApplicationStatusCommand(id, request.status());
        return JobApplicationResponse.from(service.updateStatus(command));
    }
}
