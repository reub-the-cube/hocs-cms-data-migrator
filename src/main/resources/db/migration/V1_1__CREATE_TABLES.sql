DROP TABLE IF EXISTS documents cascade;

CREATE TABLE IF NOT EXISTS documents
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    NUMERIC,
    document_extracted         BOOLEAN,
    document_id                NUMERIC,
    failure_reason             TEXT,
    temp_file_name             TEXT
);

DROP TABLE IF EXISTS extraction_stages cascade;

CREATE TABLE IF NOT EXISTS extraction_stages
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    NUMERIC,
    extracted                  BOOLEAN,
    stage                      TEXT,
    error                      TEXT,
    error_message              TEXT
);
