CREATE TABLE IF NOT EXISTS organization (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    organization_id BIGINT REFERENCES organization (id)
);

CREATE TABLE IF NOT EXISTS ticket (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    requester_id BIGINT REFERENCES users (id),
    assigned_agent_id BIGINT REFERENCES users (id),
    organization_id BIGINT REFERENCES organization (id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS ticket_comment (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT REFERENCES ticket (id),
    author_id BIGINT REFERENCES users (id),
    message TEXT NOT NULL,
    internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS ticket_history (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT REFERENCES ticket (id),
    changed_by_id BIGINT REFERENCES users (id),
    field VARCHAR(100) NOT NULL,
    old_value VARCHAR(255),
    new_value VARCHAR(255),
    changed_at TIMESTAMP NOT NULL
);
