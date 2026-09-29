package com.josyantl.joblens.activity.interfaces.rest;

import com.josyantl.joblens.activity.application.ApplicationActivityService;
import com.josyantl.joblens.activity.domain.model.ApplicationNote;
import com.josyantl.joblens.shared.application.ApplicationActivityType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}")
@RequiredArgsConstructor
@Validated
public class ApplicationActivityController {
    private final ApplicationActivityService service;

    public record ActivityPageResponse(List<ApplicationActivityResponse> content, int page, int size,
                                       long totalElements, int totalPages) {}
    public record NoteRequest(@NotBlank @Size(max = 5000) String content) {}
    public record UpdateNoteRequest(@NotBlank @Size(max = 5000) String content,
                                    @NotNull @PositiveOrZero Long version) {}
    public record NoteResponse(Long id, Long applicationId, String content, Instant createdAt,
                               Instant updatedAt, long version) {
        static NoteResponse from(ApplicationNote value) {
            return new NoteResponse(value.getId(), value.getApplicationId(), value.getContent(),
                    value.getCreatedAt(), value.getUpdatedAt(), value.getVersion());
        }
    }

    @GetMapping("/activities")
    public ActivityPageResponse activities(@PathVariable @Positive Long applicationId,
            @RequestParam(required = false) ApplicationActivityType type,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var result = service.findActivities(applicationId, type, page, size);
        return new ActivityPageResponse(result.content().stream().map(ApplicationActivityResponse::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/notes")
    public List<NoteResponse> notes(@PathVariable @Positive Long applicationId) {
        return service.findNotes(applicationId).stream().map(NoteResponse::from).toList();
    }

    @PostMapping("/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse createNote(@PathVariable @Positive Long applicationId,
                                   @Valid @RequestBody NoteRequest request) {
        return NoteResponse.from(service.createNote(applicationId, request.content()));
    }

    @PutMapping("/notes/{noteId}")
    public NoteResponse updateNote(@PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long noteId, @Valid @RequestBody UpdateNoteRequest request) {
        return NoteResponse.from(service.updateNote(applicationId, noteId, request.content(), request.version()));
    }

    @DeleteMapping("/notes/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long noteId, @RequestParam @PositiveOrZero long version) {
        service.deleteNote(applicationId, noteId, version);
    }
}
