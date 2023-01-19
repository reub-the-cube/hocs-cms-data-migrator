DROP TABLE IF EXISTS risk_assessment cascade;

CREATE TABLE IF NOT EXISTS risk_assessment
(
    id                         BIGSERIAL PRIMARY KEY,
    caseid                     NUMERIC,
    priority                   TEXT,
    fromoraffectingachild      TEXT
);
