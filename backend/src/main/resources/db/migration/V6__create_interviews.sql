CREATE TABLE interviews (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    round_number INTEGER NOT NULL CHECK (round_number BETWEEN 1 AND 100),
    type VARCHAR(20) NOT NULL CHECK (type IN ('PHONE', 'VIDEO', 'ONSITE', 'OTHER')),
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes BETWEEN 1 AND 480),
    contact VARCHAR(200) NOT NULL,
    meeting_url VARCHAR(2000) NOT NULL,
    location VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED')),
    questions TEXT NOT NULL,
    summary TEXT NOT NULL,
    next_steps TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    CONSTRAINT chk_interview_completion CHECK (
        (status = 'COMPLETED' AND completed_at IS NOT NULL) OR
        (status <> 'COMPLETED' AND completed_at IS NULL)
    )
);

CREATE INDEX idx_interviews_application_start ON interviews(application_id, starts_at, id);
CREATE INDEX idx_interviews_status_start ON interviews(status, starts_at, id);
CREATE INDEX idx_interviews_start ON interviews(starts_at, id);
