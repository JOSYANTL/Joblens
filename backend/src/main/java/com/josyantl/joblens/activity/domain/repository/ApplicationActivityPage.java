package com.josyantl.joblens.activity.domain.repository;

import com.josyantl.joblens.activity.domain.model.ApplicationActivity;
import java.util.List;

public record ApplicationActivityPage(List<ApplicationActivity> content, int page, int size,
                                      long totalElements, int totalPages) {}
