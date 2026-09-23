CREATE TABLE application_notes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id BIGINT NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    content VARCHAR(5000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_application_notes_application
    ON application_notes(user_id, application_id, updated_at DESC, id DESC);

CREATE TABLE application_activity_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id BIGINT NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    subject_type VARCHAR(30) NOT NULL,
    subject_id BIGINT,
    summary VARCHAR(500) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_application_activity_events_application
    ON application_activity_events(user_id, application_id, occurred_at DESC, id DESC);

INSERT INTO application_activity_events
    (user_id, application_id, type, subject_type, subject_id, summary, occurred_at)
SELECT user_id, id, 'APPLICATION_CREATED', 'APPLICATION', id, '创建了职位申请', created_at
FROM job_applications;
