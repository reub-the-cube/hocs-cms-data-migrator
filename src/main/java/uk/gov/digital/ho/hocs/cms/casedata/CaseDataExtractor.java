package uk.gov.digital.ho.hocs.cms.casedata;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.digital.ho.hocs.cms.casedata.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.cms.casedata.repository.CaseDataRowMapper;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CaseDataExtractor {

    private final DataSource dataSource;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final CaseDataRepository caseDataRepository;

    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_CASE_DATA = """ 
            select casereference, ukbareceiveddate, casesladate, initialtype, currenttype,
            queuename, location, nrocombo, CLOSED_DT, owningcsu, businessarea, status
             from FLODS_UKBACOMPLAINTS_D00 where caseid = :caseId""";

    // Use lgncc_closedcasehdr.otherdescription if status is closed or lgncc_casehdr.otherdescription if status is open
    private final String FETCH_CLOSED_CASE_DESCRIPTION = "select otherdescription from lgncc_closedcasehdr where caseid = :caseId";

    private final String FETCH_OPEN_CASE_DESCRIPTION = "select otherdescription from lgncc_casehdr where caseid = :caseId";

    public CaseDataExtractor(DataSource dataSource, CaseDataRepository caseDataRepository,
                             NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.dataSource = dataSource;
        this.caseDataRepository = caseDataRepository;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public CaseData getCaseData(BigDecimal caseId) {
        CaseData caseData = jdbcTemplate.queryForObject(FETCH_CASE_DATA, new CaseDataRowMapper(), caseId);

        // lgncc_closedcasehdr.otherdescription if status is closed or lgncc_casehdr.otherdescription if status is open

        if (caseData.getStatus().equalsIgnoreCase("closed")) {
            caseData.setDescription(jdbcTemplate.queryForObject(FETCH_CLOSED_CASE_DESCRIPTION, String.class, caseId));
        } else {
            caseData.setDescription(jdbcTemplate.queryForObject(FETCH_OPEN_CASE_DESCRIPTION, String.class, caseId));
        }
        return caseData;
    }
}
