package uk.gov.digital.ho.hocs.cms.casedata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataComplaint;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataComplaintsRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;

@Component
@Slf4j
public class CaseDataComplaintExtractor {

    private final DataSource dataSource;

    private final CaseDataComplaintsRepository caseDataComplaintsRepository;

    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_CASE_DATA = """ 
            select casereference, ukbareceiveddate, casesladate, initialtype, currenttype,
            queuename, location, nrocombo, CLOSED_DT, owningcsu, businessarea, status
             from FLODS_UKBACOMPLAINTS_D00 where caseid = ?
             """;

    // Use lgncc_closedcasehdr.otherdescription if status is closed or lgncc_casehdr.otherdescription if status is open
    private final String FETCH_CLOSED_CASE_DESCRIPTION = "select otherdescription from lgncc_closedcasehdr where caseid = ?";

    private final String FETCH_OPEN_CASE_DESCRIPTION = "select otherdescription from lgncc_casehdr where caseid = ?";

    public CaseDataComplaintExtractor(@Qualifier("cms") DataSource dataSource, CaseDataComplaintsRepository caseDataComplaintsRepository) {
        this.dataSource = dataSource;
        this.caseDataComplaintsRepository = caseDataComplaintsRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Transactional
    public void getCaseDataComplaint(BigDecimal caseId) {

        caseDataComplaintsRepository.deleteAllByCaseId(caseId);

        CaseDataComplaint caseDataComplaint = jdbcTemplate.queryForObject(FETCH_CASE_DATA, (rs, rowNum) -> {
            CaseDataComplaint cd = new CaseDataComplaint();
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
            if (caseDataComplaint.getStatus().equalsIgnoreCase("closed")) {
                caseDataComplaint.setDescription(jdbcTemplate.queryForObject(FETCH_CLOSED_CASE_DESCRIPTION, String.class, caseId));
            } else {
                caseDataComplaint.setDescription(jdbcTemplate.queryForObject(FETCH_OPEN_CASE_DESCRIPTION, String.class, caseId));
            }
        }
        catch (DataAccessException e) {
            // query for object throws exception if no rows returned so we add empty string to description
            log.error("No Case Data description for Case ID: {}", caseId);
            caseDataComplaint.setDescription("");
        }

        // persist case data
        caseDataComplaint.setCaseId(caseId);
        caseDataComplaintsRepository.save(caseDataComplaint);

    }

    private String convertDateToString(Date date) {
        return (date != null) ? date.toLocalDate().toString() : "";
    }
}
