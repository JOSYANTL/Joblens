package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.application.service.InterviewService;
import com.josyantl.joblens.job.domain.model.InterviewFeedback;
import com.josyantl.joblens.job.domain.model.InterviewStatus;
import com.josyantl.joblens.job.domain.repository.InterviewPage;
import com.josyantl.joblens.job.domain.repository.InterviewQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class InterviewController {
    private final InterviewService service;

    public record UpdateRequest(@NotNull @Valid InterviewDetailsRequest details,
                                @NotNull @PositiveOrZero Long version) {}
    public record StatusRequest(@NotNull InterviewStatus status,
                                @NotNull @PositiveOrZero Long version) {}
    public record FeedbackRequest(@Size(max = 10000) String questions,
            @Size(max = 10000) String summary, @Size(max = 10000) String nextSteps,
            @NotNull @PositiveOrZero Long version) {}
    public record PageResponse(List<InterviewResponse> content, int page, int size,
                               long totalElements, int totalPages) {
        public static PageResponse from(InterviewPage result) {
            return new PageResponse(result.content().stream().map(InterviewResponse::from).toList(),
                    result.page(), result.size(), result.totalElements(), result.totalPages());
        }
    }

    @PostMapping("/applications/{applicationId}/interviews")
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse schedule(@PathVariable @Positive Long applicationId,
                                      @Valid @RequestBody InterviewDetailsRequest request) {
        return InterviewResponse.from(service.schedule(applicationId, request.toDomain()));
    }

    @GetMapping("/applications/{applicationId}/interviews/{interviewId}")
    public InterviewResponse get(@PathVariable @Positive Long applicationId,
                                 @PathVariable @Positive Long interviewId) {
        return InterviewResponse.from(service.get(applicationId, interviewId));
    }

    @PutMapping("/applications/{applicationId}/interviews/{interviewId}")
    public InterviewResponse reschedule(@PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long interviewId, @Valid @RequestBody UpdateRequest request) {
        return InterviewResponse.from(service.reschedule(applicationId, interviewId,
                request.details().toDomain(), request.version()));
    }

    @PatchMapping("/applications/{applicationId}/interviews/{interviewId}/status")
    public InterviewResponse changeStatus(@PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long interviewId, @Valid @RequestBody StatusRequest request) {
        return InterviewResponse.from(service.changeStatus(applicationId, interviewId,
                request.status(), request.version()));
    }

    @PutMapping("/applications/{applicationId}/interviews/{interviewId}/feedback")
    public InterviewResponse feedback(@PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long interviewId, @Valid @RequestBody FeedbackRequest request) {
        return InterviewResponse.from(service.recordFeedback(applicationId, interviewId,
                new InterviewFeedback(request.questions(), request.summary(), request.nextSteps()), request.version()));
    }

    @GetMapping("/interviews")
    public PageResponse search(
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) InterviewStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(service.search(new InterviewQuery(applicationId, status, from, to, page, size)));
    }

    @GetMapping("/interviews/upcoming")
    public PageResponse upcoming(@RequestParam(required = false) Long applicationId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Instant now = Instant.now();
        return PageResponse.from(service.search(new InterviewQuery(applicationId,
                InterviewStatus.SCHEDULED, now, now.plus(7, ChronoUnit.DAYS), page, size)));
    }
}
