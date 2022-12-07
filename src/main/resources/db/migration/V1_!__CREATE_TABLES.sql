DROP TABLE IF EXISTS documents cascade;

CREATE TABLE IF NOT EXISTS cms.documents
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    INT,
    document_extracted         BOOLEAN,
    document_id                INT,
    failure_reason             TEXT,
    temp_file_name             TEXT
);

DROP TABLE IF EXISTS complaints cascade;

CREATE TABLE IF NOT EXISTS cms.complaints
(
    id                         BIGSERIAL PRIMARY KEY,
    case_id                    INT,
    complaint_extracted        BOOLEAN,
    stage                      TEXT
);
