package uk.gov.digital.ho.hocs.cms.treatofficial;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.cms.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.cms.domain.exception.LogEvent;
import uk.gov.digital.ho.hocs.cms.domain.model.CaseDataTreatOfficial;
import uk.gov.digital.ho.hocs.cms.domain.repository.CaseDataTreatOfficialsRepository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;

@Component
@Slf4j
public class CaseDataTreatOfficialExtractor {

    private final DataSource dataSource;

    private final CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository;

    private final JdbcTemplate jdbcTemplate;

    private final String FETCH_CASE_DATA_CLOSEDCASEVIEW = "select OpenedDateTime, TypeID, allocatedToDeptID, CaseRef, targetFixDateTime, otherDescription, Title, closedDateTime, Severity, Priority, CaseStatus, TargetDate from LGNCC_CLOSEDCASEVIEW where CaseId = ?";

    private final String FETCH_CASE_DATA_TREATOFFICIALEFORM = "SELECT letterTopic, ResponseDate, tx_rejectnotes FROM LGNEF_EFORMINSTANCEVERSION INNER JOIN LGNES_TreatOfficialEform ON LGNEF_EFORMINSTANCEVERSION.datarecordid = LGNES_TreatOfficialEform.uniqueid WHERE LGNEF_EFORMINSTANCEVERSION.caseid = ?";


    public CaseDataTreatOfficialExtractor(@Qualifier("cms") DataSource dataSource, CaseDataTreatOfficialsRepository caseDataTreatOfficialsRepository) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.caseDataTreatOfficialsRepository = caseDataTreatOfficialsRepository;
    }

    @Transactional
    public void getCaseDataTreatOfficial(BigDecimal caseId) {

        caseDataTreatOfficialsRepository.deleteAllByCaseId(caseId);

        CaseDataTreatOfficial caseDataTreatOfficial = jdbcTemplate.queryForObject(FETCH_CASE_DATA_CLOSEDCASEVIEW, (rs, rowNum) -> {
            CaseDataTreatOfficial caseData = new CaseDataTreatOfficial();
            caseData.setOpenedDateTime(convertDateToString(rs.getDate("OpenedDateTime")));
            caseData.setTypeId(rs.getString("TypeID"));
            caseData.setAllocatedToDeptId(rs.getString("allocatedToDeptID"));
            caseData.setCaseRef(rs.getString("CaseRef"));
            caseData.setTargetFixDateTime(convertDateToString(rs.getDate("targetFixDateTime")));
            caseData.setOtherDescription(rs.getString("otherDescription"));
            caseData.setTitle(rs.getString("Title"));
            caseData.setClosedDateTime(convertDateToString(rs.getDate("closedDateTime")));
            caseData.setSeverity(rs.getBigDecimal("Severity"));
            caseData.setPriority(rs.getBigDecimal("Priority"));
            caseData.setStatus(rs.getBigDecimal("CaseStatus"));
            caseData.setTargetDate(convertDateToString(rs.getDate("TargetDate")));
            return caseData;
        }, caseId);

        HashMap<String, String> eFormQueryValues;
        try {
            eFormQueryValues = jdbcTemplate.queryForObject(FETCH_CASE_DATA_TREATOFFICIALEFORM, (rs, rowNum) -> {
                HashMap<String, String> query = new HashMap<>();
                query.put("letterTopic",rs.getString("letterTopic"));
                query.put("ResponseDate",rs.getString("ResponseDate"));
                query.put("tx_rejectnotes",rs.getString("tx_rejectnotes"));
                return query;
            }, caseId);

            caseDataTreatOfficial.setLetterTopic(eFormQueryValues.get("letterTopic"));
            caseDataTreatOfficial.setResponseDate(eFormQueryValues.get("ResponseDate"));
            caseDataTreatOfficial.setTxRejectNotes(eFormQueryValues.get("tx_rejectnotes"));
        } catch (DataAccessException e) {
            log.error("Couldn't retrieve case-data from TREATOFFICIALEFORM for CASE ID {}. Error message: {}", caseId, e.getMessage());
        }

        caseDataTreatOfficial.setCaseId(caseId);
        caseDataTreatOfficialsRepository.save(caseDataTreatOfficial);
    }

    private String convertDateToString(Date date) {
        return (date != null) ? date.toLocalDate().toString() : "";
    }
}