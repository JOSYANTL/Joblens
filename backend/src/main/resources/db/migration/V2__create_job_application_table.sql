ALTER TABLE job_application
    RENAME TO job_applications;

ALTER TABLE job_applications
    RENAME COLUMN title TO position;

ALTER TABLE job_applications
    ADD COLUMN updated_at TIMESTAMP;

UPDATE job_applications
SET updated_at = created_at
WHERE updated_at IS NULL;

ALTER TABLE job_applications
    ALTER COLUMN updated_at SET NOT NULL;