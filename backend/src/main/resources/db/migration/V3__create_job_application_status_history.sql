CREATE TABLE job_application_status_history
(
    id                 BIGSERIAL PRIMARY KEY,
    job_application_id BIGINT      NOT NULL,
    from_status        VARCHAR(30),
    to_status          VARCHAR(30) NOT NULL,
    changed_at         TIMESTAMP   NOT NULL,
    CONSTRAINT fk_status_history_job_application
        FOREIGN KEY (job_application_id)
        REFERENCES job_applications (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_status_history_job_application
    ON job_application_status_history (job_application_id, changed_at, id);

CREATE INDEX idx_job_applications_status
    ON job_applications (status);

CREATE INDEX idx_job_applications_updated_at
    ON job_applications (updated_at DESC);
