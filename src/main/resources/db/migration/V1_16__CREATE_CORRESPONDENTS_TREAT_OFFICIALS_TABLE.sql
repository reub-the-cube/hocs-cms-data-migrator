DROP TABLE IF EXISTS correspondents_treat_officials cascade;

CREATE TABLE IF NOT EXISTS correspondents_treat_officials
(
    id                  BIGSERIAL PRIMARY KEY,
    caseid              Numeric,
    correspondentid     Numeric,
    isprimary           BOOLEAN
);