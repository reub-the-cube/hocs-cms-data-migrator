DROP TABLE IF EXISTS case_data cascade;

CREATE TABLE IF NOT EXISTS case_data
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