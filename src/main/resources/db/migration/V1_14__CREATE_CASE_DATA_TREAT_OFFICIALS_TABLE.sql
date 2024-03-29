ALTER TABLE complaints
    RENAME COLUMN complaint_extracted TO extracted;

ALTER TABLE complaints
    RENAME TO extraction_stages;

ALTER TABLE case_data
    RENAME TO case_data_complaints;

DROP TABLE IF EXISTS case_data_treat_officials cascade;

CREATE TABLE IF NOT EXISTS case_data_treat_officials
(
    id                         BIGSERIAL PRIMARY KEY,
    typeid                     Numeric,
    lettertopic                TEXT,
    openeddatetime             TEXT,
    allocatedtodeptid          Numeric,
    responsedate               TEXT,
    tx_rejectnotes             TEXT,
    caseref                    TEXT,
    targetfixdatetime          TEXT,
    otherdescription           TEXT,
    title                      TEXT,
    closeddatetime             TEXT,
    severity                   TEXT,
    priority                   TEXT,
    status                     TEXT
);