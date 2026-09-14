package com.josyantl.joblens.job.application.command;

public record UpdateJobApplicationCommand(
        Long id,
        String company,
        String position,
        String description
) {
}
