CREATE TABLE follow_up_tasks (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    notes TEXT NOT NULL,
    due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('TODO', 'DONE', 'CANCELLED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0 CHECK (version >= 0),
    CONSTRAINT chk_task_completion CHECK (
        (status = 'DONE' AND completed_at IS NOT NULL) OR
        (status <> 'DONE' AND completed_at IS NULL)
    )
);

CREATE INDEX idx_tasks_application_due ON follow_up_tasks(application_id, due_at, id);
CREATE INDEX idx_tasks_status_due ON follow_up_tasks(status, due_at, id);
CREATE INDEX idx_tasks_due ON follow_up_tasks(due_at, id);
