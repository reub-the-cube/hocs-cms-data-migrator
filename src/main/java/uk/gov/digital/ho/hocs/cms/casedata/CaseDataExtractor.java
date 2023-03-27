package uk.gov.digital.ho.hocs.cms.casedata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseData;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;

@Component
@Slf4j
public class CaseDataExtractor {

    private final DataSource dataSource;

    private final CaseDataRepository caseDataRepository;

    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_CASE_DATA = """ 
            select casereference, ukbareceiveddate, casesladate, initialtype, currenttype,
            queuename, location, nrocombo, CLOSED_DT, owningcsu, businessarea, status
             from FLODS_UKBACOMPLAINTS_D00 where caseid = ?
             """;

    // Use lgncc_closedcasehdr.otherdescription if status is closed or lgncc_casehdr.otherdescription if status is open
    private final String FETCH_CLOSED_CASE_DESCRIPTION = "select otherdescription from lgncc_closedcasehdr where caseid = ?";

    private final String FETCH_OPEN_CASE_DESCRIPTION = "select otherdescription from lgncc_casehdr where caseid = ?";

    public CaseDataExtractor(@Qualifier("cms") DataSource dataSource, CaseDataRepository caseDataRepository) {
        this.dataSource = dataSource;
        this.caseDataRepository = caseDataRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    @Transactional
    public void getCaseData(BigDecimal caseId) {

        caseDataRepository.deleteAllByCaseId(caseId);

        CaseData caseData = jdbcTemplate.queryForObject(FETCH_CASE_DATA, (rs, rowNum) -> {
            CaseData cd = new CaseData();
            cd.setCaseReference(rs.getString("casereference"));
            cd.setReceiveDate(convertDateToString(rs.getDate("ukbareceiveddate")));
            cd.setSlaDate(convertDateToString((rs.getDate("casesladate"))));
            cd.setInitialType(rs.getString("initialtype"));
            cd.setCurrentType(rs.getString("currenttype"));
            cd.setQueueName(rs.getString("queuename"));
            cd.setLocation(rs.getString("location"));
            cd.setNroCombo(rs.getString("nrocombo"));
            cd.setClosedDt(convertDateToString(rs.getDate("CLOSED_DT")));
            cd.setOwningCsu(rs.getString("owningcsu"));
            cd.setBusinessArea(rs.getString("businessarea"));
            cd.setStatus(rs.getString("status"));
            return cd;
        },  caseId);

        // lgncc_closedcasehdr.otherdescription if status is closed or lgncc_casehdr.otherdescription if status is open
        try {
            if (caseData.getStatus().equalsIgnoreCase("closed")) {
                caseData.setDescription(jdbcTemplate.queryForObject(FETCH_CLOSED_CASE_DESCRIPTION, String.class, caseId));
            } else {
                caseData.setDescription(jdbcTemplate.queryForObject(FETCH_OPEN_CASE_DESCRIPTION, String.class, caseId));
            }
        }
        catch (DataAccessException e) {
            // query for object throws exception if no rows returned so we add empty string to description
            log.error("No Case Data description for Case ID: {}", caseId);
            caseData.setDescription("");
        }

        // persist case data
        caseData.setCaseId(caseId);
        caseDataRepository.save(caseData);

    }

    private String convertDateToString(Date date) {
        return (date != null) ? date.toLocalDate().toString() : "";
    }
}
