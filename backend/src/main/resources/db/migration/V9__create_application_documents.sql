CREATE TABLE application_documents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id BIGINT NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('RESUME', 'JOB_DESCRIPTION', 'OTHER')),
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0 AND size_bytes <= 10485760),
    storage_key VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_application_documents_application
    ON application_documents(user_id, application_id, created_at DESC, id DESC);
