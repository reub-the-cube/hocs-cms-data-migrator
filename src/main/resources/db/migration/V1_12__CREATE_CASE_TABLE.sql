DROP TABLE IF EXISTS cases cascade;

CREATE TABLE IF NOT EXISTS cases
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    NUMERIC,
    representative_id          NUMERIC,
    complainant_id             NUMERIC
);