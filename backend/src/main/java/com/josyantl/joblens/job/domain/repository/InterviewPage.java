package com.josyantl.joblens.job.domain.repository;

import com.josyantl.joblens.job.domain.model.Interview;
import java.util.List;

public record InterviewPage(List<Interview> content, int page, int size,
                            long totalElements, int totalPages) {}
