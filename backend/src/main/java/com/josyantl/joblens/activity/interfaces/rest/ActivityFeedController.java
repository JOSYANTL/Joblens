package com.josyantl.joblens.activity.interfaces.rest;

import com.josyantl.joblens.activity.application.ApplicationActivityService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Validated
public class ActivityFeedController {
    private final ApplicationActivityService service;

    @GetMapping("/recent")
    public List<ApplicationActivityResponse> recent(
            @RequestParam(defaultValue = "8") @Min(1) @Max(50) int size
    ) {
        return service.findRecentActivities(size).stream()
                .map(ApplicationActivityResponse::from)
                .toList();
    }
}
