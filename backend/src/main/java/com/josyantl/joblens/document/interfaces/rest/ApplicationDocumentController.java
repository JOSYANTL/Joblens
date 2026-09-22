package com.josyantl.joblens.document.interfaces.rest;

import com.josyantl.joblens.document.application.ApplicationDocumentService;
import com.josyantl.joblens.document.domain.model.ApplicationDocument;
import com.josyantl.joblens.document.domain.model.ApplicationDocumentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/documents")
@RequiredArgsConstructor
public class ApplicationDocumentController {
    private final ApplicationDocumentService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDocumentResponse upload(@PathVariable Long applicationId,
            @RequestParam ApplicationDocumentType type, @RequestPart MultipartFile file) throws IOException {
        return ApplicationDocumentResponse.from(service.upload(applicationId, type,
                file.getOriginalFilename(), file.getBytes()));
    }

    @GetMapping
    public List<ApplicationDocumentResponse> findAll(@PathVariable Long applicationId) {
        return service.findAll(applicationId).stream().map(ApplicationDocumentResponse::from).toList();
    }

    @GetMapping("/{documentId}/content")
    public ResponseEntity<byte[]> download(@PathVariable Long applicationId, @PathVariable Long documentId) {
        var downloaded = service.download(applicationId, documentId);
        var document = downloaded.metadata();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .contentLength(document.getSizeBytes())
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(document.getOriginalFileName(), StandardCharsets.UTF_8).build().toString())
                .body(downloaded.content());
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long applicationId, @PathVariable Long documentId) {
        service.delete(applicationId, documentId);
    }

    public record ApplicationDocumentResponse(Long id, Long applicationId,
            ApplicationDocumentType type, String originalFileName, String contentType,
            long sizeBytes, java.time.Instant createdAt, String downloadUrl) {
        static ApplicationDocumentResponse from(ApplicationDocument document) {
            return new ApplicationDocumentResponse(document.getId(), document.getApplicationId(),
                    document.getType(), document.getOriginalFileName(), document.getContentType(),
                    document.getSizeBytes(), document.getCreatedAt(), "/api/applications/"
                    + document.getApplicationId() + "/documents/" + document.getId() + "/content");
        }
    }
}
