DROP TABLE IF EXISTS compensation cascade;

CREATE TABLE IF NOT EXISTS compensation
(
    id                         BIGSERIAL PRIMARY KEY,
    caseid                     NUMERIC,
    dateofcompensationclaim    TEXT,
    offeraccepted              TEXT,
    dateofpayment              TEXT,
    compensationamount         NUMERIC,
    amountclaimed              NUMERIC,
    amountoffered              NUMERIC,
    consolatorypayment         NUMERIC
 );
