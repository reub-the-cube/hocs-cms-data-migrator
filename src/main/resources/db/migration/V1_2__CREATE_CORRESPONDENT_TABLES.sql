DROP TABLE IF EXISTS address cascade;

CREATE TABLE IF NOT EXISTS address
(
    addressid                  Numeric PRIMARY KEY,
    number                     TEXT,
    addressline1               TEXT,
    addressline2               TEXT,
    addressline3               TEXT,
    addressline4               TEXT,
    addressline5               TEXT,
    addressline6               TEXT,
    postcode                   TEXT,

    CONSTRAINT addressid_idempotent UNIQUE (addressid)

);

DROP TABLE IF EXISTS individual;

CREATE TABLE IF NOT EXISTS individual
(
  partyid                    NUMERIC PRIMARY KEY,
  addressid                  NUMERIC,
  caseid                     NUMERIC,
  forename                   TEXT,
  surname                    TEXT,
  dateofbirth                DATE,
  nationality                TEXT,
  telephone                  TEXT,
  email                      TEXT,
  primarycorrespondent       BOOLEAN,
  type                       TEXT,

  CONSTRAINT partyid_idempotent UNIQUE (partyid),
  CONSTRAINT fk_address_id FOREIGN KEY (addressid) REFERENCES address (addressid)

);

DROP TABLE IF EXISTS reference;

CREATE TABLE IF NOT EXISTS reference
(
  referenceid                NUMERIC PRIMARY KEY,
  partyid                    NUMERIC,
  reftype                    TEXT,
  reference                  TEXT,

  CONSTRAINT referenceid_idempotent UNIQUE (referenceid),
  CONSTRAINT fk_individual_id FOREIGN KEY (partyid) REFERENCES individual (partyid)
);
