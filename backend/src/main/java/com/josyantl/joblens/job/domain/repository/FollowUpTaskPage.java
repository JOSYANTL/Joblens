package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.FollowUpTask;
import java.util.List;

public record FollowUpTaskPage(List<FollowUpTask> content, int page, int size,
                               long totalElements, int totalPages) {}
