DROP TABLE IF EXISTS case_links cascade;

CREATE TABLE IF NOT EXISTS case_links
(
    id                         BIGSERIAL PRIMARY KEY,
    source_case_id             NUMERIC,
    target_case_id             NUMERIC,
    description                TEXT
);
