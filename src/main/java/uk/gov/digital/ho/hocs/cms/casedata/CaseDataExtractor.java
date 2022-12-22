package uk.gov.digital.ho.hocs.cms.casedata;

import javax.sql.DataSource;

public class CaseDataExtractor {

    private final DataSource dataSource;

    private final String FETCH_CASE_DATA = """ 
            select casereference, ukbareceiveddate, casesladate, initialtype, currenttype,
            queuename, location, nrocombo, CLOSED_DT, owningcsu, businessarea, status
             from FLODS_UKBACOMPLAINTS_D00 where """;

    // Use lgncc_closedcasehdr.otherdescription if status is closed or lgncc_casehdr.otherdescription if status is open
    private final String FETCH_CLOSED_CASE_DESCRIPTION = "select otherdescription from lgncc_closedcasehdr where caseid = :caseId";

    private final String FETCH_OPEN_CASE_DESCRIPTION = "select otherdescription from lgncc_casehdr where caseid = :caseId";

    public CaseDataExtractor(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
