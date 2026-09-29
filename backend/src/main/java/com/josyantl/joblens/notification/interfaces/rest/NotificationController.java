package com.josyantl.joblens.notification.interfaces.rest;

import com.josyantl.joblens.notification.application.service.NotificationService;
import com.josyantl.joblens.notification.domain.model.Notification;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {
    private final NotificationService service;

    public record Response(Long id, Long applicationId, String type, String sourceType,
            Long sourceId, String title, String message, Instant eventAt, Instant createdAt,
            Instant readAt, String targetUrl) {
        static Response from(Notification value) {
            String segment = value.getSourceType().name().equals("INTERVIEW") ? "interviews" : "tasks";
            String url = "/applications/" + value.getApplicationId() + "/" + segment + "/" + value.getSourceId();
            return new Response(value.getId(), value.getApplicationId(), value.getType().name(),
                    value.getSourceType().name(), value.getSourceId(), value.getTitle(), value.getMessage(),
                    value.getEventAt(), value.getCreatedAt(), value.getReadAt(), url);
        }
    }
    public record PageResponse(List<Response> content, int page, int size,
                               long totalElements, int totalPages) {}
    public record UnreadCountResponse(long count) {}

    @GetMapping
    public PageResponse search(@RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var result = service.search(unreadOnly, page, size);
        return new PageResponse(result.content().stream().map(Response::from).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount() {
        return new UnreadCountResponse(service.countUnread());
    }

    @PatchMapping("/{id}/read")
    public Response markRead(@PathVariable @Positive Long id) {
        return Response.from(service.markRead(id));
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead() {
        service.markAllRead();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        service.delete(id);
    }
}
