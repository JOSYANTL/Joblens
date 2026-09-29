CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    application_id BIGINT NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    type VARCHAR(40) NOT NULL CHECK (type IN ('INTERVIEW_UPCOMING', 'TASK_DUE_SOON', 'TASK_OVERDUE')),
    source_type VARCHAR(20) NOT NULL CHECK (source_type IN ('INTERVIEW', 'TASK')),
    source_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    message VARCHAR(500) NOT NULL,
    event_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    read_at TIMESTAMP WITH TIME ZONE,
    deduplication_key VARCHAR(200) NOT NULL,
    CONSTRAINT uk_notifications_deduplication_key UNIQUE (deduplication_key)
);

CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at DESC, id DESC);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, read_at, created_at DESC);

