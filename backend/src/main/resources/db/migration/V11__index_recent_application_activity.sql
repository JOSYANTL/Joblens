CREATE INDEX idx_application_activity_events_user_recent
    ON application_activity_events(user_id, occurred_at DESC, id DESC);
