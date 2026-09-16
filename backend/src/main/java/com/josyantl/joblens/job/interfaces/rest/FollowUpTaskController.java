package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.application.service.FollowUpTaskService;
import com.josyantl.joblens.job.domain.model.FollowUpTaskStatus;
import com.josyantl.joblens.job.domain.repository.FollowUpTaskQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
public class FollowUpTaskController {
    private final FollowUpTaskService service;

    public record CreateRequest(@NotBlank @Size(max = 200) String title,
            @Size(max = 10000) String notes, @NotNull Instant dueAt) {}
    public record UpdateRequest(@NotBlank @Size(max = 200) String title,
            @Size(max = 10000) String notes, @NotNull Instant dueAt,
            @NotNull @PositiveOrZero Long version) {}
    public record StatusRequest(@NotNull FollowUpTaskStatus status,
            @NotNull @PositiveOrZero Long version) {}
    public record PageResponse(List<FollowUpTaskResponse> content, int page, int size,
                               long totalElements, int totalPages) {}

    @PostMapping("/applications/{applicationId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public FollowUpTaskResponse create(@PathVariable @Positive Long applicationId,
                                      @Valid @RequestBody CreateRequest request) {
        return FollowUpTaskResponse.from(service.create(applicationId, request.title(),
                request.notes(), request.dueAt()), Instant.now());
    }

    @GetMapping("/applications/{applicationId}/tasks/{taskId}")
    public FollowUpTaskResponse get(@PathVariable @Positive Long applicationId,
                                   @PathVariable @Positive Long taskId) {
        return FollowUpTaskResponse.from(service.get(applicationId, taskId), Instant.now());
    }

    @GetMapping("/tasks")
    public PageResponse search(
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) FollowUpTaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueTo,
            @RequestParam(defaultValue = "false") boolean overdueOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Instant now = Instant.now();
        var result = service.search(new FollowUpTaskQuery(applicationId, status, dueFrom,
                dueTo, overdueOnly, page, size), now);
        return new PageResponse(result.content().stream()
                .map(task -> FollowUpTaskResponse.from(task, now)).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @PutMapping("/applications/{applicationId}/tasks/{taskId}")
    public FollowUpTaskResponse update(@PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long taskId, @Valid @RequestBody UpdateRequest request) {
        return FollowUpTaskResponse.from(service.update(applicationId, taskId, request.title(),
                request.notes(), request.dueAt(), request.version()), Instant.now());
    }

    @PatchMapping("/applications/{applicationId}/tasks/{taskId}/status")
    public FollowUpTaskResponse changeStatus(@PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long taskId, @Valid @RequestBody StatusRequest request) {
        return FollowUpTaskResponse.from(service.changeStatus(applicationId, taskId,
                request.status(), request.version()), Instant.now());
    }

    @DeleteMapping("/applications/{applicationId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long applicationId,
                       @PathVariable @Positive Long taskId,
                       @RequestParam @PositiveOrZero long version) {
        service.delete(applicationId, taskId, version);
    }
}
