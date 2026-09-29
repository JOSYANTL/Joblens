package com.josyantl.joblens.shared.application;

public interface ApplicationDataCleanup {
    void beforeApplicationDeleted(Long applicationId, Long userId);
}
