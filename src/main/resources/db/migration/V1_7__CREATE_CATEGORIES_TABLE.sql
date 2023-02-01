DROP TABLE IF EXISTS categories cascade;

CREATE TABLE IF NOT EXISTS categories
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    NUMERIC,
    category                   TEXT,
    selected                   TEXT,
    substantiated              TEXT,
    amount                     NUMERIC
);
