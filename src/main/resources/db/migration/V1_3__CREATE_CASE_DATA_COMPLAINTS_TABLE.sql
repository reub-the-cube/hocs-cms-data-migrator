DROP TABLE IF EXISTS case_data_complaints cascade;

CREATE TABLE IF NOT EXISTS case_data_complaints
(
    id                         BIGSERIAL PRIMARY KEY,
    caseid                     Numeric,
    casereference              TEXT,
    ukbareceivedate            TEXT,
    casesladate                TEXT,
    initialtype                TEXT,
    currenttype                TEXT,
    queuename                  TEXT,
    location                   TEXT,
    nrocombo                   TEXT,
    closedt                    TEXT,
    owningcsu                  TEXT,
    businessarea               TEXT,
    status                     TEXT,
    description                TEXT
 );
