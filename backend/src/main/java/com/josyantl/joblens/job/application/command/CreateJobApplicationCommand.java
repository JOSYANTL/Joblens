package com.josyantl.joblens.job.application.command;

public record CreateJobApplicationCommand(
        String company,
        String position,
        String description
) {
}
