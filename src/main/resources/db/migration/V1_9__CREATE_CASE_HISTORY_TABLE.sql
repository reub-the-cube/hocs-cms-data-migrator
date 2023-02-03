DROP TABLE IF EXISTS case_history cascade;

CREATE TABLE IF NOT EXISTS case_history
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    NUMERIC,
    type                       TEXT,
    description                TEXT,
    created_by                 TEXT,
    created                    DATE
);
