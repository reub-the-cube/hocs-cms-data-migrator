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

DROP TABLE IF EXISTS complaints cascade;

CREATE TABLE IF NOT EXISTS complaints
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    NUMERIC,
    complaint_extracted        BOOLEAN,
    stage                      TEXT
);
