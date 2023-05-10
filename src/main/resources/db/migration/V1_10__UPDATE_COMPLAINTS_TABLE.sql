ALTER TABLE extraction_stages
ADD COLUMN extraction_id UUID NULL;

DROP TABLE IF EXISTS progress cascade;

CREATE TABLE IF NOT EXISTS progress
(
    id                         BIGSERIAL PRIMARY KEY,
    extraction_id              UUID,
    success                    NUMERIC,
    failure                    NUMERIC
);

