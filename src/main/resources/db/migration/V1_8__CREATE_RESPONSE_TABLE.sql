DROP TABLE IF EXISTS cresponse cascade;

CREATE TABLE IF NOT EXISTS response
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    NUMERIC,
    response                   TEXT
);
