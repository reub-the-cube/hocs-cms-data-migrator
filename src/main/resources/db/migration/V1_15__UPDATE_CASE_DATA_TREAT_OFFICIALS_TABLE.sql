ALTER TABLE case_data_treat_officials
    ADD COLUMN caseid Numeric NOT NULL;

ALTER TABLE case_data_treat_officials
ALTER COLUMN allocatedtodeptid TYPE TEXT,
ALTER COLUMN typeid TYPE TEXT,
ALTER COLUMN severity TYPE Numeric USING severity::numeric,
ALTER COLUMN priority TYPE Numeric USING priority::numeric,
ALTER COLUMN status TYPE Numeric USING status::numeric;