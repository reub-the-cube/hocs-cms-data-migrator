CREATE INDEX idx_cases_case_id
    ON cases(case_id);

CREATE INDEX idx_individual_partyid
    ON individual(partyid);

CREATE INDEX idx_address_addressid
    ON address(addressid);

CREATE INDEX idx_reference_partyid
    ON reference(partyid);

CREATE INDEX idx_case_data_complaints_caseid
    ON case_data_complaints(caseid);

CREATE INDEX idx_compensation_caseid
    ON compensation(caseid);

CREATE INDEX idx_categories_case_id
    ON categories(case_id);

CREATE INDEX idx_risk_assessment_caseid
    ON risk_assessment(caseid);

CREATE INDEX idx_response_case_id
    ON response(case_id);

CREATE INDEX idx_case_links_source_case_id
    ON case_links(source_case_id);

CREATE INDEX idx_case_links_target_case_id
    ON case_links(target_case_id);

CREATE INDEX idx_case_history_case_id
    ON case_history(case_id);

