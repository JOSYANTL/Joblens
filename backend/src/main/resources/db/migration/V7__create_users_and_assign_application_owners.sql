CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_lowercase CHECK (email = LOWER(email))
);

-- Existing local data is preserved under a disabled account. It cannot be used to sign in.
INSERT INTO users (email, password_hash, display_name, enabled, created_at)
VALUES ('legacy@joblens.local', '{noop}disabled-account', 'Legacy data', FALSE, CURRENT_TIMESTAMP);

ALTER TABLE job_applications
    ADD COLUMN user_id BIGINT;

UPDATE job_applications
SET user_id = (SELECT id FROM users WHERE email = 'legacy@joblens.local')
WHERE user_id IS NULL;

ALTER TABLE job_applications
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE job_applications
    ADD CONSTRAINT fk_job_applications_user
        FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_job_applications_user_id ON job_applications (user_id);
CREATE INDEX idx_job_applications_user_status ON job_applications (user_id, status);
